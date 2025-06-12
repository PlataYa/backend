import random
import time
import uuid
import threading
from locust import HttpUser, SequentialTaskSet, task, between
from typing import Optional, Dict, Any, Set

# --- Helper Functions for Data Generation ---
USER_COUNTER = 0
CVU_COUNTER = int(time.time() * 1000)  # Usar timestamp como base para CVUs únicos
USER_LOCK = threading.Lock()  # Lock dedicado para generar emails únicos
CVU_LOCK = threading.Lock()

def generate_unique_email():
    """Genera un email único de forma thread-safe."""
    global USER_COUNTER
    with USER_LOCK:
        USER_COUNTER += 1
        # Combinamos contador + timestamp + sufijo aleatorio para minimizar colisiones
        unique_suffix = f"{USER_COUNTER}_{int(time.time() * 1000)}_{random.randint(1000, 9999)}"
        return f"testuser{unique_suffix}@example.com"

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
    """
    Pool compartido de CVUs válidos para transferencias P2P.
    Thread-safe para uso en pruebas distribuidas.
    """
    def __init__(self):
        self.valid_cvus: Set[int] = set()
        self._lock = threading.Lock()

    def add_cvu(self, cvu: int) -> None:
        """Agrega un CVU válido al pool de forma thread-safe."""
        with self._lock:
            if cvu is not None:
                self.valid_cvus.add(cvu)
                print(f"🏦 CVU {cvu} agregado al pool (Total: {len(self.valid_cvus)})")

    def get_random_cvu(self, exclude_cvu: int) -> Optional[int]:
        """Obtiene un CVU aleatorio del pool, excluyendo el CVU especificado."""
        with self._lock:
            available_cvus = [cvu for cvu in self.valid_cvus if cvu != exclude_cvu]
            if not available_cvus:
                return None
            return random.choice(available_cvus)

    def get_pool_size(self) -> int:
        """Retorna el número de CVUs en el pool."""
        with self._lock:
            return len(self.valid_cvus)

# Instancia global del pool de usuarios
user_pool = UserPool()

class WalletUserFlow(SequentialTaskSet):
    """
    TaskSet secuencial que simula el flujo completo de un usuario de billetera virtual.
    Mantiene el estado del usuario y ejecuta las tareas en orden específico.
    """
    
    def on_start(self):
        """Inicialización del estado del usuario."""
        # Estado del usuario
        self.user_email = generate_unique_email()
        self.user_password = "TestPassword124!"
        self.user_name = "Test"
        self.user_lastname = "User"
        self.user_day_of_birth = "1990-01-01"
        self.auth_token: Optional[str] = None
        self.cvu: Optional[int] = None
        self.balance: float = 0.0
        
        print(f"🚀 Iniciando flujo para usuario: {self.user_email}")

    def get_auth_headers(self) -> Dict[str, str]:
        """Retorna los headers de autenticación para las requests autenticadas."""
        if self.auth_token:
            return {"Authorization": f"Bearer {self.auth_token}"}
        return {}

    @task
    def step_1_register_user(self):
        """Registra un nuevo usuario y obtiene el CVU inicial."""
        # Evitar reintentar registro si ya tenemos token (ya se registró previamente)
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
            if response.ok:  # Usar response.ok en lugar de verificar solo código 200
                try:
                    data = response.json()
                    self.cvu = data.get("cvu")
                    self.auth_token = data.get("token")
                    
                    if not self.cvu or not self.auth_token:
                        response.failure("Registration response missing cvu or token")
                        print("⚠️ Respuesta incompleta:", data)
                    else:
                        user_pool.add_cvu(self.cvu)  # Agregar al pool global
                        response.success()
                        print(f"✅ Usuario registrado - CVU: {self.cvu}")
                except Exception as e:
                    response.failure(f"Failed to parse register response: {e}")
                    print(f"❌ Error al parsear respuesta: {e}")
            else:
                response.failure(f"Registration failed with status {response.status_code}: {response.text}")
                print(f"❌ Error en registro ({response.status_code}): {response.text}")
                print("📝 Datos enviados:", payload)

    @task
    def step_2_login_user(self):
        """Autentica al usuario y actualiza el token."""
        if not self.user_email:
            print("⚠️ Login omitido - Email no disponible")
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
                        print(f"✅ Login exitoso - Token actualizado")
                        # Realizar depósito inicial automático
                        self._make_initial_deposit()
                    else:
                        response.failure("Token o CVU faltante en respuesta de login")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de login: {e}")
            else:
                response.failure(f"Login falló con código {response.status_code}: {response.text}")

    def _make_initial_deposit(self):
        """Realiza un depósito inicial automático después del login."""
        if not self.cvu or not self.auth_token:
            print("⚠️ No se puede realizar depósito inicial - CVU o token faltante")
            return
            
        initial_amount = 1000.0  # Monto inicial fijo
        
        payload = {
            "sourceCvu": 200000000001,  # Actualizado según ExternalTransactionDTO
            "destinationCvu": self.cvu,  # Para depósito, ambos CVUs son el mismo
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
            if response.status_code in [200, 201]:  # Aceptar tanto 200 como 201
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance = initial_amount
                        response.success()
                        print(f"✅ Depósito inicial realizado: ${initial_amount}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de depósito: {e}")
            else:
                response.failure(f"Depósito inicial falló ({response.status_code}): {response.text}")

    @task
    def step_3_check_balance(self):
        """Verifica el saldo actual de la billetera."""
        if not self.cvu or not self.auth_token:
            print("⚠️ Consulta de saldo omitida - CVU o token faltante")
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
                    print(f"✅ Saldo actual: ${self.balance}")
                except Exception as e:
                    response.failure(f"Error al parsear saldo: {e}")
            else:
                response.failure(f"Consulta de saldo falló: {response.text}")

    @task
    def step_4_make_deposit(self):
        """Realiza un depósito en la billetera."""
        if not self.cvu or not self.auth_token:
            print("⚠️ Depósito omitido - CVU o token faltante")
            return
            
        amount = generate_random_amount()
        
        payload = {
            "sourceCvu": 200000000001,  # Actualizado según ExternalTransactionDTO
            "destinationCvu": self.cvu,  # Para depósito, ambos CVUs son el mismo
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
            if response.status_code in [200, 201]:  # Aceptar tanto 200 como 201
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance += amount
                        response.success()
                        print(f"✅ Depósito realizado: ${amount} - Nuevo saldo: ${self.balance}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de depósito: {e}")
            else:
                response.failure(f"Depósito falló ({response.status_code}): {response.text}")

    @task
    def step_5_make_withdrawal(self):
        """Realiza un retiro de la billetera."""
        if not self.cvu or not self.auth_token:
            print("⚠️ Retiro omitido - CVU o token faltante")
            return
            
        if self.balance <= 10.0:  # Mínimo de $10 para retiro
            print(f"⚠️ Retiro omitido - Saldo insuficiente: ${self.balance}")
            return
            
        # Retirar entre el 10% y 30% del saldo actual
        max_withdrawal = min(self.balance * 0.3, self.balance - 10.0)  # Dejar al menos $10
        withdrawal_amount = round(random.uniform(10.0, max_withdrawal), 2)
        
        payload = {
            "sourceCvu": self.cvu,  # Actualizado según ExternalTransactionDTO
            "destinationCvu": 200000000001,  # Para retiro, ambos CVUs son el mismo
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
            if response.status_code in [200, 201]:  # Aceptar tanto 200 como 201
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance -= withdrawal_amount
                        response.success()
                        print(f"✅ Retiro realizado: ${withdrawal_amount} - Nuevo saldo: ${self.balance}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de retiro: {e}")
            else:
                response.failure(f"Retiro falló ({response.status_code}): {response.text}")

    @task
    def step_6_make_p2p_transfer(self):
        """Realiza una transferencia P2P a otro usuario."""
        if not self.cvu or not self.auth_token:
            print("⚠️ Transferencia omitida - CVU o token faltante")
            return
            
        if self.balance <= 20.0:  # Mínimo de $20 para transferencia
            print(f"⚠️ Transferencia omitida - Saldo insuficiente: ${self.balance}")
            return
            
        # Obtener un CVU válido del pool
        destination_cvu = user_pool.get_random_cvu(self.cvu)
        if not destination_cvu:
            print("⚠️ Transferencia omitida - No hay CVUs disponibles")
            return
            
        # Transferir entre el 5% y 15% del saldo actual
        max_transfer = min(self.balance * 0.15, self.balance - 10.0)  # Dejar al menos $10
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
            if response.status_code in [200, 201]:  # Aceptar tanto 200 como 201
                try:
                    response_data = response.json()
                    if response_data.get("status") == "COMPLETED":
                        self.balance -= transfer_amount
                        response.success()
                        print(f"✅ Transferencia realizada: ${transfer_amount} a CVU {destination_cvu}")
                    else:
                        response.failure(f"Estado de transacción inesperado: {response_data.get('status')}")
                except Exception as e:
                    response.failure(f"Error al parsear respuesta de transferencia: {e}")
            elif response.status_code == 404:
                response.success()  # CVU no encontrado es un caso válido
                print(f"ℹ️ CVU destino no encontrado - Error esperado")
            else:
                response.failure(f"Transferencia falló ({response.status_code}): {response.text}")

    @task
    def step_7_get_transaction_history(self):
        """Consulta el historial de transacciones."""
        if not self.cvu or not self.auth_token:
            print("⚠️ Consulta de historial omitida - CVU o token faltante")
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
                    print(f"✅ Historial consultado: {transaction_count} transacciones")
                except Exception as e:
                    response.failure(f"Error al parsear historial: {e}")
            else:
                response.failure(f"Consulta de historial falló ({response.status_code}): {response.text}")

    def on_stop(self):
        """Limpieza al finalizar el flujo."""
        print(f"🏁 Flujo completado para usuario: {self.user_email}")
        print(f"   CVU: {self.cvu}")
        print(f"   Saldo final: ${self.balance}")
        


class WalletUser(HttpUser):
    """Usuario que ejecuta el flujo completo de la billetera virtual."""
    tasks = [WalletUserFlow]
    wait_time = between(1, 3)  # Tiempo de espera entre tareas
    host = "http://localhost:8080"  # Host por defecto


class StressWalletUser(HttpUser):
    """Usuario para pruebas de estrés con tiempos de espera mínimos."""
    tasks = [WalletUserFlow]
    wait_time = between(0.1, 0.5)  # Tiempo de espera mínimo
    host = "http://localhost:8080"  # Host por defecto


