# Запуск WiFi VK Access Control

## Требования
- Java 11+ (JRE/JDK)
- Maven 3.6+
- SQLite3

## Настройка конфигурации

Перед запуском приложения обновите файл `src/main/resources/application.yml`:

### 1. Конфигурация роутера
```yaml
router:
  host: 192.168.1.1          # IP адрес вашего роутера
  ssh-port: 22               # SSH порт
  username: admin            # Логин к роутеру
  password: password          # Пароль к роутеру
  wifi-interface: wlan0      # Интерфейс Wi-Fi (уточните на вашем роутере)
```

### 2. Конфигурация VK API
```yaml
vk-api:
  access-token: YOUR_VK_GROUP_ACCESS_TOKEN  # Token от ВК группы
  group-id: 123456789                       # ID вашей группы ВК
  api-version: 5.131
  base-url: https://api.vk.com/method
```

### 3. Конфигурация Proxy сервера
```yaml
proxy:
  port: 8888                 # Порт для перехвата трафика
  listen-address: 0.0.0.0    # Адрес прослушивания
```

## Запуск приложения

### Способ 1: Используя скрипт (Windows)
Просто запустите:
```
start.bat
```

### Способ 2: Ручной запуск с Maven
```
mvn clean install
mvn spring-boot:run
```

### Способ 3: Из JAR файла
```
mvn clean package
java -jar target/wifi-vk-access-control-1.0.0.jar
```

## Проверка работы

После запуска приложение будет доступно на:
- **REST API**: http://localhost:8080/api
- **Proxy сервер**: http://localhost:8888

### Тестовые запросы

#### Проверить доступ
```
GET /api/auth/check-access?macAddress=AA:BB:CC:DD:EE:FF&ipAddress=192.168.1.100&url=http://example.com
```

#### Привязать MAC адрес к VK ID
```
POST /api/auth/link-device?macAddress=AA:BB:CC:DD:EE:FF&vkUserId=123456789
```

#### Получить информацию о пользователе
```
GET /api/auth/user?macAddress=AA:BB:CC:DD:EE:FF
GET /api/auth/user-by-vk?vkUserId=123456789
```

## Режим разработки

Если роутер не доступен при разработке, приложение запустится в режиме разработки с логированием:

```yaml
dev:
  mode: true
  skip-router-connection: true
```

## Логи

Логи сохраняются в файл: `logs/wifi-access.log`

## Остановка приложения

Нажмите `Ctrl+C` в консоли или закройте окно.

## Распространённые ошибки

### "Could not find artifact org.hibernate.dialect:hibernate-dialect-sqlite"
- Зависимость была заменена на `com.github.gwenn:sqlite-dialect`
- Очистите кеш Maven: `mvn clean`

### Нет подключения к роутеру
- Проверьте IP адрес роутера в конфиге
- Убедитесь, что SSH доступен на роутере
- Проверьте логины/пароли

### Ошибка с портами
- Порты 8080 и 8888 должны быть свободны
- Измените их в конфиге, если нужно

## Поддержка

При возникновении проблем проверьте файл логов: `logs/wifi-access.log`

