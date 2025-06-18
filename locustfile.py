import random
import time
import uuid
import threading
import logging
from locust import HttpUser, TaskSet, task, between
from typing import Optional, Dict, Any, Set

logger = logging.getLogger('wallet_test')
logger.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
ch = logging.StreamHandler()
ch.setFormatter(formatter)
logger.addHandler(ch)

USER_COUNTER = 0
CVU_COUNTER = int(time.time() * 1000)
USER_LOCK = threading.Lock()
CVU_LOCK = threading.Lock()

def generate_unique_email():
    global USER_COUNTER
    with USER_LOCK:
        USER_COUNTER += 1
        unique_suffix = f"{USER_COUNTER}_{int(time.time() * 1000)}_{random.randint(1000, 9999)}"
        return f"testuser{unique_suffix}@example.com"

def generate_unique_cvu():
    global CVU_COUNTER
    with CVU_LOCK:
        CVU_COUNTER += 1
        return 10000000000 + CVU_COUNTER

def generate_random_amount():
    return round(random.uniform(1.0, 1000.0), 2)

def generate_external_reference():
    return f"REF_{uuid.uuid4().hex[:8]}"

class UserPool:
    def __init__(self):
        self.valid_cvus: Set[int] = set()
        self._lock = threading.Lock()

    def add_cvu(self, cvu: int) -> None:
        with self._lock:
            if cvu is not None:
                self.valid_cvus.add(cvu)
                logger.info(f" CVU {cvu} agregado al pool (Total: {len(self.valid_cvus)})")

    def get_random_cvu(self, exclude_cvu: int) -> Optional[int]:
        with self._lock:
            available_cvus = [cvu for cvu in self.valid_cvus if cvu != exclude_cvu]
            if not available_cvus:
                return None
            return random.choice(available_cvus)

    def get_pool_size(self) -> int:
        with self._lock:
            return len(self.valid_cvus)

user_pool = UserPool()

class WalletUserFlow(TaskSet):
    def on_start(self):
        self.user_email = generate_unique_email()
        self.user_password = "TestPassword124!"
        self.user_name = "Test"
        self.user_lastname = "User"
        self.user_day_of_birth = "1990-01-01"
        self.auth_token: Optional[str] = None
        self.cvu: Optional[int] = None
        self.balance: float = 0.0
        logger.info(f"🚀 Iniciando flujo para usuario: {self.user_email}")

    def get_auth_headers(self) -> Dict[str, str]:
        if self.auth_token:
            return {"Authorization": f"Bearer {self.auth_token}"}
        return {}

    @task(1)
    def step_1_register_user(self):
        if self.auth_token:
            return
        payload = {
            "name": self.user_name,
            "lastname": self.user_lastname,
            "mail": self.user_email,
            "password": self.user_password,
            "dayOfBirth": self.user_day_of_birth
        }
        
        with self.client.post(
            "/api/v1/user/register",
            json=payload,
            name="🔑 Register User",
            catch_response=True
        ) as response:
            if response.ok:
                try:
                    data = response.json()
                    self.cvu = data.get("cvu")
                    self.auth_token = data.get("token")
                    
                    if not self.cvu or not self.auth_token:
                        response.failure("Registration response missing cvu or token")
                        logger.warning(" Respuesta incompleta:", data)
                    else:
                        user_pool.add_cvu(self.cvu)
                        response.success()
                        logger.info(f" Usuario registrado - CVU: {self.cvu}")
                except Exception as e:
                    response.failure(f"Failed to parse register response: {e}")
                    logger.error(f" Error al parsear respuesta: {e}")
            else:
                response.failure(f"Registration failed with status {response.status_code}: {response.text}")
                logger.error(f" Error en registro ({response.status_code}): {response.text}")
                logger.debug(" Datos enviados:", payload)

    @task(1)
    def step_2_login_user(self):
        if not self.user_email:
            logger.warning(" Login omitido - Email no disponible")
            return
            
        payload = {
            "mail": self.user_email,
            "password": self.user_password
        }
        
        with self.client.post(
            "/api/v1/user/login",
            json=payload,
            name="🔐 Login User",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.auth_token = data.get("token")
                    cvu_from_login = data.get("cvu")
                    
                    if self.auth_token and cvu_from_login:
                        self.cvu = cvu_from_login
                        response.success()
                        logger.info(f" Login exitoso - Token actualizado")
                        self._make_initial_deposit()
                    else:
                        response.failure("Token o CVU faltante en respuesta de login")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de login: {e}")
            else:
                response.failure(f"Login falló con código {response.status_code}: {response.text}")

    def _make_initial_deposit(self):
        if not self.cvu or not self.auth_token:
            logger.warning("No se puede realizar depósito inicial - CVU o token faltante")
            return
            
        initial_amount = 1000.0
        
        payload = {
            "sourceCvu": 200000000001,
            "destinationCvu": self.cvu,
            "amount": initial_amount,
            "currency": "ARS",
            "externalReference": generate_external_reference()
        }
        
        with self.client.post(
            "/api/v1/transaction/deposit",
            json=payload,
            headers=self.get_auth_headers(),
            name="💰 Initial Deposit",
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance = initial_amount
                        response.success()
                        logger.info(f" Depósito inicial realizado: ${initial_amount}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de depósito: {e}")
            else:
                response.failure(f"Depósito inicial falló ({response.status_code}): {response.text}")

    @task(8)
    def step_3_check_balance(self):
        if not self.cvu or not self.auth_token:
            logger.warning("Consulta de saldo omitida - CVU o token faltante")
            return
            
        with self.client.get(
            f"/api/v1/wallet/balance/{self.cvu}",
            headers=self.get_auth_headers(),
            name="💰 Check Balance",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    self.balance = data.get("balance", 0.0)
                    response.success()
                    logger.info(f" Saldo actual: ${self.balance}")
                except Exception as e:
                    response.failure(f"Error al parsear saldo: {e}")
            else:
                response.failure(f"Consulta de saldo falló: {response.text}")

    @task(4)
    def step_4_make_deposit(self):
        if not self.cvu or not self.auth_token:
            logger.warning(" Depósito omitido - CVU o token faltante")
            return
            
        amount = generate_random_amount()
        
        payload = {
            "sourceCvu": 200000000001,
            "destinationCvu": self.cvu,
            "amount": amount,
            "currency": "ARS",
            "externalReference": generate_external_reference()
        }
        
        with self.client.post(
            "/api/v1/transaction/deposit",
            json=payload,
            headers=self.get_auth_headers(),
            name="💵 Make Deposit",
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance += amount
                        response.success()
                        logger.info(f" Depósito realizado: ${amount} - Nuevo saldo: ${self.balance}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de depósito: {e}")
            else:
                response.failure(f"Depósito falló ({response.status_code}): {response.text}")

    @task(2)
    def step_5_make_withdrawal(self):
        if not self.cvu or not self.auth_token:
            logger.warning(" Retiro omitido - CVU o token faltante")
            return
            
        if self.balance <= 10.0:
            logger.warning(f" Retiro omitido - Saldo insuficiente: ${self.balance}")
            return
            
        max_withdrawal = min(self.balance * 0.3, self.balance - 10.0)
        withdrawal_amount = round(random.uniform(10.0, max_withdrawal), 2)
        
        payload = {
            "sourceCvu": self.cvu,
            "destinationCvu": 200000000001,
            "amount": withdrawal_amount,
            "currency": "ARS",
            "externalReference": generate_external_reference()
        }
        
        with self.client.post(
            "/api/v1/transaction/withdrawal",
            json=payload,
            headers=self.get_auth_headers(),
            name="💸 Make Withdrawal",
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance -= withdrawal_amount
                        response.success()
                        logger.info(f" Retiro realizado: ${withdrawal_amount} - Nuevo saldo: ${self.balance}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de retiro: {e}")
            else:
                response.failure(f"Retiro falló ({response.status_code}): {response.text}")

    @task(3)
    def step_6_make_p2p_transfer(self):
        if not self.cvu or not self.auth_token:
            logger.warning(" Transferencia omitida - CVU o token faltante")
            return
            
        if self.balance <= 20.0:
            logger.warning(f" Transferencia omitida - Saldo insuficiente: ${self.balance}")
            return
            
        destination_cvu = user_pool.get_random_cvu(self.cvu)
        if not destination_cvu:
            logger.warning(" Transferencia omitida - No hay CVUs disponibles")
            return
            
        max_transfer = min(self.balance * 0.15, self.balance - 10.0)
        transfer_amount = round(random.uniform(10.0, max_transfer), 2)
        
        payload = {
            "payerCvu": self.cvu,
            "payeeCvu": destination_cvu,
            "amount": transfer_amount,
            "currency": "ARS"
        }
        
        with self.client.post(
            "/api/v1/transaction/transfer",
            json=payload,
            headers=self.get_auth_headers(),
            name="🔄 P2P Transfer",
            catch_response=True
        ) as response:
            if response.status_code in [200, 201]:
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance -= transfer_amount
                        response.success()
                        logger.info(f" Transferencia realizada: ${transfer_amount} a CVU {destination_cvu}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de transferencia: {e}")
            elif response.status_code == 404:
                response.success()
                logger.info(f" CVU destino no encontrado - Error esperado")
            else:
                response.failure(f"Transferencia falló ({response.status_code}): {response.text}")

    @task(5)
    def step_7_get_transaction_history(self):
        if not self.cvu or not self.auth_token:
            logger.warning(" Consulta de historial omitida - CVU o token faltante")
            return
            
        with self.client.get(
            f"/api/v1/transaction/{self.cvu}/history",
            headers=self.get_auth_headers(),
            name="📊 Transaction History",
            catch_response=True
        ) as response:
            if response.status_code == 200:
                try:
                    data = response.json()
                    transaction_count = len(data) if isinstance(data, list) else 0
                    response.success()
                    logger.info(f" Historial consultado: {transaction_count} transacciones")
                except Exception as e:
                    response.failure(f"Error al parsear historial: {e}")
            else:
                response.failure(f"Consulta de historial falló ({response.status_code}): {response.text}")

    def on_stop(self):
        logger.info(f"🏁 Flujo completado para usuario: {self.user_email}")
        logger.info(f"   CVU: {self.cvu}")
        logger.info(f"   Saldo final: ${self.balance}")

class WalletUser(HttpUser):
    tasks = [WalletUserFlow]
    wait_time = between(1, 3)
    host = "http://localhost:8080"

class StressWalletUser(HttpUser):
    tasks = [WalletUserFlow]
    wait_time = between(0.1, 0.5)
    host = "http://localhost:8080"

