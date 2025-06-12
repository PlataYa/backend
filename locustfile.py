import random
from locust import HttpUser, task, between
import uuid
from collections import deque
from typing import Set
import threading
import time

# --- Helper Functions for Data Generation ---
USER_COUNTER = 0
CVU_COUNTER = int(time.time() * 1000)  # Usar timestamp como base para CVUs únicos
CVU_LOCK = threading.Lock()

def generate_unique_email():
    global USER_COUNTER
    USER_COUNTER += 1
    return f"testuser{USER_COUNTER}_{random.randint(1000, 9999)}@example.com"

def generate_unique_cvu():
    global CVU_COUNTER
    with CVU_LOCK:
        CVU_COUNTER += 1
        # Asegurarse de que el CVU tenga 11 dígitos como mínimo
        return 10000000000 + CVU_COUNTER

def generate_random_amount():
    # Genera montos aleatorios entre 1.0 y 1000.0
    return round(random.uniform(1.0, 1000.0), 2)

def generate_external_reference():
    # Genera una referencia externa única para depósitos y retiros
    return f"REF_{uuid.uuid4().hex[:8]}"

class UserPool:
    def __init__(self):
        self.valid_cvus: Set[int] = set()
        self._lock = threading.Lock()

    def add_cvu(self, cvu: int) -> None:
        with self._lock:
            if cvu is not None:
                self.valid_cvus.add(cvu)

    def get_random_cvu(self, exclude_cvu: int) -> int:
        with self._lock:
            available_cvus = [cvu for cvu in self.valid_cvus if cvu != exclude_cvu]
            if not available_cvus:
                return None
            return random.choice(available_cvus)

# Crear una instancia global del pool de usuarios
user_pool = UserPool()

class BaseApiUser(HttpUser): # Renamed from ApiUser
    # host will be defined in subclasses or overridden by CLI
    abstract = True # Prevents Locust from picking up BaseApiUser directly

    def on_start(self):
        """Called when a User starts. Use for login or setup."""
        self.user_email = generate_unique_email()
        self.user_password = "TestPassword123!"
        self.user_name = "Test"
        self.user_lastname = "User"
        self.user_day_of_birth = "1990-01-01" # Adjust format if needed
        self.cvu = None 
        self.auth_token = None
        self.balance = 0.0

        self.register()
        if self.cvu:  # Solo intentar login si el registro fue exitoso
            self.login()
            if self.auth_token:  # Solo hacer depósito inicial si el login fue exitoso
                self.initial_deposit()

    def register(self):
        payload = {
            "name": self.user_name,
            "lastname": self.user_lastname,
            "mail": self.user_email,
            "password": self.user_password,
            "dayOfBirth": self.user_day_of_birth
        }
        with self.client.post("/api/v1/user/register", json=payload, catch_response=True) as response:
            if response.ok:
                try:
                    response_data = response.json()
                    self.cvu = response_data.get("cvu")
                    self.auth_token = response_data.get("token")
                    if not self.cvu or not self.auth_token:
                        response.failure("Registration response missing cvu or token")
                    else:
                        user_pool.add_cvu(self.cvu)
                        response.success()
                except Exception as e:
                    response.failure(f"Failed to parse register response: {e}")
            else:
                response.failure(f"Registration failed with status {response.status_code}: {response.text}")

    def login(self):
        payload = {
            "mail": self.user_email,
            "password": self.user_password
        }
        with self.client.post("/api/v1/user/login", json=payload, catch_response=True) as response:
            if response.ok:
                try:
                    response_data = response.json()
                    self.auth_token = response_data.get("token")
                    self.cvu = response_data.get("cvu")
                    
                    if not self.auth_token:
                        response.failure("'token' field missing in login response")
                        return
                    
                    if not self.cvu:
                        response.failure("'cvu' field missing in login response")
                        return
                    
                    response.success()
                except Exception as e:
                    response.failure(f"Failed to parse login response: {e}")
            else:
                response.failure(f"Login failed with status {response.status_code}: {response.text}")

    def get_auth_headers(self):
        if self.auth_token:
            return {"Authorization": f"Bearer {self.auth_token}"}
        return {}

    def initial_deposit(self):
        """Realiza un depósito inicial para tener fondos para las pruebas"""
        amount = 1000.0  # Monto inicial fijo para pruebas
        self.deposit(amount)
        self.balance = amount

    # --- Transaction Operations ---
    def deposit(self, amount):
        payload = {
            "payeeCvu": self.cvu,
            "amount": amount,
            "currency": "ARS",
            "externalReference": generate_external_reference()
        }
        with self.client.post(
            "/api/v1/transaction/deposit",
            json=payload,
            headers=self.get_auth_headers(),
            name="/api/v1/transaction/deposit",
            catch_response=True
        ) as response:
            if response.ok:
                self.balance += amount
                response.success()
            else:
                response.failure(f"Deposit failed: {response.text}")

    def withdraw(self, amount):
        if amount > self.balance:
            return  # Skip if insufficient funds
        
        payload = {
            "payerCvu": self.cvu,
            "amount": amount,
            "currency": "ARS",
            "externalReference": generate_external_reference()
        }
        with self.client.post(
            "/api/v1/transaction/withdraw",
            json=payload,
            headers=self.get_auth_headers(),
            name="/api/v1/transaction/withdraw",
            catch_response=True
        ) as response:
            if response.ok:
                self.balance -= amount
                response.success()

    def p2p_transfer(self, destination_cvu, amount):
        if amount > self.balance:
            return  # Skip if insufficient funds
            
        payload = {
            "payerCvu": self.cvu,
            "payeeCvu": destination_cvu,
            "amount": amount,
            "currency": "ARS"
        }
        with self.client.post(
            "/api/v1/transaction/transfer",
            json=payload,
            headers=self.get_auth_headers(),
            name="/api/v1/transaction/transfer",
            catch_response=True
        ) as response:
            if response.ok:
                self.balance -= amount
                response.success()

    # --- Tasks ---
    @task(2)
    def check_balance(self):
        if not self.cvu:
            return
        with self.client.get(
            f"/api/v1/wallet/balance/{self.cvu}",
            headers=self.get_auth_headers(),
            name="/api/v1/wallet/balance/[cvu]",
            catch_response=True
        ) as response:
            if response.ok:
                try:
                    balance_data = response.json()
                    self.balance = balance_data.get("balance", self.balance)
                    response.success()
                except Exception as e:
                    response.failure(f"Failed to parse balance response: {e}")

    @task(3)
    def make_deposit(self):
        if not self.cvu:
            return
        amount = generate_random_amount()
        self.deposit(amount)

    @task(2)
    def make_withdrawal(self):
        if not self.cvu or self.balance <= 0:
            return
        amount = generate_random_amount() % self.balance  # Asegura que no exceda el balance
        self.withdraw(amount)

    @task(3)
    def make_p2p_transfer(self):
        if not self.cvu or self.balance <= 0:
            return

        # Obtener un CVU válido aleatorio del pool
        destination_cvu = user_pool.get_random_cvu(self.cvu)
        if not destination_cvu:
            return  # No hay otros CVUs disponibles para transferir
        
        amount = min(generate_random_amount(), self.balance * 0.8)  # Usa hasta 80% del balance
        self.p2p_transfer(destination_cvu, amount)

    @task(2)
    def get_transaction_history(self):
        if not self.cvu:
            return
        with self.client.get(
            f"/api/v1/transaction/{self.cvu}/history",
            headers=self.get_auth_headers(),
            name="/api/v1/transaction/[cvu]/history",
            catch_response=True
        ) as response:
            if not response.ok:
                response.failure(f"Failed to get transaction history: {response.text}")

    @task(1)
    def validate_random_cvu(self):
        if not self.cvu:
            return
        random_cvu = random.randint(10000000000, 9000000000000000000)
        with self.client.get(
            f"/api/v1/wallet/valid/cvu?cvu={random_cvu}",
            headers=self.get_auth_headers(),
            name="/api/v1/wallet/valid/cvu",
            catch_response=True
        ) as response:
            if response.ok:
                response.success()
            else:
                response.failure(f"CVU validation failed: {response.text}")

    @task(1)
    def re_login(self):
        """Ocasionalmente refresca el token de autenticación"""
        self.login()

# --- User Profile for Load Testing ---
class LoadTestUser(BaseApiUser):
    wait_time = between(1, 3)  # Simula tiempo de pensamiento del usuario
    host = "http://localhost:8080" # Default host, can be overridden by --host CLI arg

# --- User Profile for Stress Testing ---
class StressTestUser(BaseApiUser):
    wait_time = between(0.1, 0.5) # Minimal think time for aggressive testing
    host = "http://localhost:8080" # Default host, can be overridden by --host CLI arg

# To run with specific users, you can specify them in the Locust UI when starting a test,
# or list them in the command line:
# locust -f locustfile.py LoadTestUser StressTestUser
# locust -f locustfile.py LoadTestUser
# locust -f locustfile.py StressTestUser
#
# If no users are specified on the command line, Locust will find all HttpUser subclasses
# (that don't have abstract=True) and make them available to choose from in the UI. 