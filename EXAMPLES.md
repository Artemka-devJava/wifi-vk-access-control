## Примеры использования API

### 1. Проверка доступа пользователя

```bash
curl -X GET "http://localhost:8080/api/auth/check-access?macAddress=AA:BB:CC:DD:EE:FF&ipAddress=192.168.1.100&url=http://google.com"
```

Ответ (подписан):
```json
{
  "allowed": true,
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "vkUserId": 123456789,
  "isSubscribed": true,
  "userId": 1,
  "reason": "subscribed"
}
```

Ответ (не подписан):
```json
{
  "allowed": false,
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "vkUserId": 123456789,
  "isSubscribed": false,
  "userId": 1,
  "reason": "not_subscribed"
}
```

### 2. Связывание устройства с VK пользователем

```bash
curl -X POST "http://localhost:8080/api/auth/link-device?macAddress=AA:BB:CC:DD:EE:FF&vkUserId=123456789"
```

Ответ:
```
Device linked successfully
```

### 3. Получение информации об устройстве

```bash
curl -X GET "http://localhost:8080/api/auth/user?macAddress=AA:BB:CC:DD:EE:FF"
```

Ответ:
```json
{
  "id": 1,
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "vkUserId": 123456789,
  "isSubscribed": true,
  "lastCheckTime": 1715000000,
  "firstSeenTime": 1714900000,
  "lastSeenTime": 1715000000,
  "deviceName": "Unknown",
  "createdAt": "2024-05-05T10:30:00",
  "updatedAt": "2024-05-05T12:00:00"
}
```

### 4. Получение общей статистики

```bash
curl -X GET "http://localhost:8080/api/stats/summary"
```

Ответ:
```json
{
  "total_devices": 42,
  "subscribed_devices": 35,
  "unsubscribed_devices": 7
}
```

### 5. Получение списка всех устройств

```bash
curl -X GET "http://localhost:8080/api/stats/devices?page=0&size=10"
```

### 6. Получение логов доступа для устройства

```bash
curl -X GET "http://localhost:8080/api/stats/access-logs?macAddress=AA:BB:CC:DD:EE:FF"
```

Ответ:
```json
[
  {
    "id": 1,
    "userId": 1,
    "macAddress": "AA:BB:CC:DD:EE:FF",
    "action": "allowed",
    "ipAddress": "192.168.1.100",
    "requestedUrl": "http://google.com",
    "reason": "subscribed",
    "timestamp": "2024-05-05T12:00:00"
  },
  {
    "id": 2,
    "userId": 1,
    "macAddress": "AA:BB:CC:DD:EE:FF",
    "action": "allowed",
    "ipAddress": "192.168.1.100",
    "requestedUrl": "http://youtube.com",
    "reason": "subscribed",
    "timestamp": "2024-05-05T12:05:00"
  }
]
```

## Файловая структура проекта

```
wifi-vk-access-control/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/wifi/access/
│   │   │       ├── WifiAccessControlApp.java          # Главный класс приложения
│   │   │       ├── config/                            # Конфигурационные классы
│   │   │       │   ├── ProxyConfig.java
│   │   │       │   ├── RouterConfig.java
│   │   │       │   ├── SubscriptionConfig.java
│   │   │       │   └── VkApiConfig.java
│   │   │       ├── controller/                        # REST контроллеры
│   │   │       │   ├── AuthController.java            # Аутентификация и связывание
│   │   │       │   ├── StatsController.java           # Статистика и логи
│   │   │       │   └── GlobalExceptionHandler.java    # Обработка ошибок
│   │   │       ├── dto/                               # Data Transfer Objects
│   │   │       │   ├── AccessCheckResult.java         # Результат проверки доступа
│   │   │       │   ├── LinkDeviceRequest.java         # Запрос связывания
│   │   │       │   └── ApiResponse.java               # Общий ответ API
│   │   │       ├── entity/                            # JPA сущности
│   │   │       │   ├── User.java                      # Пользователь/устройство
│   │   │       │   ├── AccessLog.java                 # Логи доступа
│   │   │       │   └── Notification.java              # Отправленные уведомления
│   │   │       ├── exception/                         # Пользовательские исключения
│   │   │       │   ├── RouterException.java
│   │   │       │   └── VkApiException.java
│   │   │       ├── proxy/                             # Прокси сервер (Netty)
│   │   │       │   ├── ProxyServer.java               # Netty сервер
│   │   │       │   └── ProxyHandler.java              # Обработчик запросов
│   │   │       ├── repository/                        # Репозитории JPA
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── AccessLogRepository.java
│   │   │       │   └── NotificationRepository.java
│   │   │       ├── router/                            # Управление роутером
│   │   │       │   ├── DeviceManager.java             # Управление устройствами
│   │   │       │   ├── RouterConnectionManager.java   # SSH подключение
│   │   │       │   └── TrafficInterceptor.java        # Перехват трафика (iptables)
│   │   │       ├── service/                           # Бизнес-логика
│   │   │       │   ├── AccessController.java          # Проверка доступа
│   │   │       │   └── ApplicationInitializationService.java  # Инициализация
│   │   │       └── vk/                                # Интеграция с VK API
│   │   │           ├── VkApiClient.java               # HTTP клиент
│   │   │           ├── VkSubscriptionChecker.java     # Проверка подписки
│   │   │           └── VkNotificationSender.java      # Отправка сообщений
│   │   └── resources/
│   │       ├── application.yml                        # Конфигурация приложения
│   │       └── db/
│   │           └── schema.sql                         # Схема БД
│   └── test/                                          # Тесты
└── pom.xml                                            # Конфигурация Maven
```

## Поток обработки HTTP запроса

```
1. HTTP запрос от клиента (браузер Wi-Fi пользователя)
   ↓
2. iptables перенаправляет порт 80 на 8888 (Proxy Server)
   ↓
3. ProxyServer (Netty) получает запрос
   ↓
4. ProxyHandler.channelRead0() обрабатывает запрос
   ↓
5. Извлечение MAC адреса по IP из ARP таблицы
   ↓
6. Вызов AccessController.checkAccess()
   ├─ Получение/создание пользователя в БД (DeviceManager)
   ├─ Проверка наличия VK ID
   │  ├─ Если нет → перенаправить на Captive Portal
   │  └─ Если да → продолжить
   ├─ Проверка подписки через VkSubscriptionChecker
   │  ├─ Сначала проверить кеш
   │  └─ Если кеша нет → вызвать VkApiClient.isMember()
   ├─ Если подписан → разрешить доступ
   └─ Если не подписан → отправить уведомление и заблокировать
   ↓
7. Логирование результата в БД (AccessLog)
   ↓
8. Возврат результата (разрешить/запретить трафик)
```

## Интеграция с VK API

### Методы, используемые приложением:

#### groups.isMember
Проверяет, является ли пользователь членом группы.

```
Запрос:
https://api.vk.com/method/groups.isMember?
  group_id=123456789
  &user_id=987654321
  &access_token=YOUR_TOKEN
  &v=5.131

Ответ (подписан):
{
  "response": 1
}

Ответ (не подписан):
{
  "response": 0
}
```

#### messages.send
Отправляет личное сообщение от группы пользователю.

```
Запрос:
https://api.vk.com/method/messages.send?
  user_id=987654321
  &message=YOUR_MESSAGE
  &access_token=YOUR_TOKEN
  &v=5.131
  &random_id=RANDOM_ID

Ответ (успешно):
{
  "response": MESSAGE_ID
}
```

## Диаграмма взаимодействия компонентов

```
┌──────────────┐
│ WiFi Users   │
└──────┬───────┘
       │ HTTP Request
       │ (Port 80)
       ▼
┌────────────────────┐
│ iptables/Firewall  │ (Redirect to 8888)
└────────┬───────────┘
         │
         ▼
┌──────────────────────────────┐
│ ProxyServer (Netty)          │
│ ├─ ProxyHandler              │
│ └─ Captive Portal Generator  │
└────────┬─────────────────────┘
         │
         ▼
┌────────────────────────────────┐
│ AccessController               │
├────────────────────────────────┤
│ • checkAccess()                │
│ • associateMacWithVkUser()     │
└────┬──────────────────────┬────┘
     │                      │
     ▼                      ▼
┌──────────────────┐  ┌──────────────────────┐
│ DeviceManager    │  │ VkSubscriptionChecker│
├──────────────────┤  ├──────────────────────┤
│ • register       │  │ • isSubscribed()     │
│ • scan devices   │  │ • cache              │
└────┬─────────────┘  └──────┬───────────────┘
     │                       │
     ▼                       ▼
┌──────────────────┐  ┌──────────────────────┐
│ TrafficInterceptor   VkNotificationSender  │
├──────────────────┤  ├──────────────────────┤
│ • iptables       │  │ • sendMessage()      │
│ • DHCP leases    │  └──────────────┬───────┘
└────────┬─────────┘                 │
         │                           │
         │                    ┌──────▼──────┐
         │                    │ VkApiClient  │
         │                    ├──────────────┤
         │                    │ • isMember   │
         │                    │ • sendMessage│
         │                    └──────┬───────┘
         │                           │
         │                           ▼
         │                    ┌──────────────┐
         │                    │ VK API       │
         │                    └──────────────┘
         │
         ▼
┌────────────────────────────────┐
│ SQLite Database                │
├────────────────────────────────┤
│ • users (MAC → VK ID)          │
│ • access_logs (статистика)     │
│ • notifications (уведомления)  │
│ • subscription_cache (кеш)     │
└────────────────────────────────┘
```

## Рекомендации для развертывания

### На OpenWRT роутере:

1. **Установите зависимости:**
```bash
opkg update
opkg install openssh-sftp-server
opkg install iptables
```

2. **Включите SSH:**
```bash
/etc/init.d/dropbear enable
/etc/init.d/dropbear start
```

3. **Настройте iptables (опционально, приложение сделает это):**
```bash
iptables -t nat -N WIFI_CONTROL_wlan0
iptables -t nat -A WIFI_CONTROL_wlan0 -p tcp --dport 80 -j REDIRECT --to-port 8888
iptables -t nat -A WIFI_CONTROL_wlan0 -p tcp --dport 443 -j REDIRECT --to-port 8888
iptables -t nat -A PREROUTING -i wlan0 -j WIFI_CONTROL_wlan0
iptables-save > /etc/iptables.rules
```

4. **Запустите приложение:**
```bash
java -jar wifi-vk-access-control-1.0.0.jar &
```

## Мониторинг и отладка

### Просмотр логов:
```bash
tail -f logs/wifi-access.log
```

### Проверка iptables правил:
```bash
iptables -t nat -L -n
```

### Проверка подключенных устройств:
```bash
arp -a
cat /tmp/dhcp.leases
```

### Тестирование VK API:
```bash
curl "https://api.vk.com/method/groups.isMember?group_id=YOUR_GROUP&user_id=YOUR_USER&access_token=YOUR_TOKEN&v=5.131"
```

