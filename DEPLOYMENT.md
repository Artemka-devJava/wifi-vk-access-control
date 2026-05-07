# DEPLOYMENT.md - Руководство по развертыванию

## Система требований

### Программное обеспечение:
- Java 11 или выше
- Maven 3.6 или выше
- OpenWRT роутер или Linux роутер с SSH доступом
- iptables для управления трафиком
- SQLite 3 (встроена в Java)

### Аппаратные требования:
- Сервер с минимум 512MB оперативной памяти для приложения
- 100MB свободного места для базы данных и логов
- Подключение к интернету для API VK

## Этапы развертывания

### 1. Подготовка окружения

#### На сервере с приложением:

```bash
# Установка Java
sudo apt-get install openjdk-11-jre-headless

# Установка Maven (опционально, если собираете на сервере)
sudo apt-get install maven

# Создание директорий
mkdir -p /opt/wifi-vk-control
mkdir -p /opt/wifi-vk-control/data
mkdir -p /opt/wifi-vk-control/logs
mkdir -p /var/lib/wifi-vk-control
```

#### На роутере (OpenWRT):

```bash
# SSH подключение к роутеру
ssh root@192.168.1.1

# Обновление пакетов
opkg update

# Установка необходимых пакетов
opkg install openssh-server
opkg install openssh-client
opkg install iptables
opkg install dnsmasq

# Проверка iptables
which iptables
which iptables-save
```

### 2. Получение VK API учетных данных

1. **Создайте сообщество (группу) в ВКонтакте:**
   - Перейдите на https://vk.com
   - Создайте новую группу
   - Запомните ID группы (видно в URL: `club123456789` → ID = `123456789`)

2. **Получите API токен:**
   - Зайдите в настройки группы (Settings)
   - Перейдите в раздел "API usage" или "Разработчикам"
   - Нажмите "Create token" или "Создать токен"
   - Выберите необходимые разрешения:
     - `messages` - для отправки сообщений
     - `groups` - для проверки подписки
   - Скопируйте полученный токен

3. **Добавьте версию API:**
   - Убедитесь, что используется версия 5.131 или совместимая
   - Проверьте в настройках API версию (Settings > API version)

### 3. Сборка приложения

```bash
# Клонирование репозитория
git clone <repository-url>
cd wifi-vk-access-control

# Сборка проекта (создаст JAR в target/)
mvn clean package

# JAR файл будет в: target/wifi-vk-access-control-1.0.0.jar
```

### 4. Конфигурация приложения

#### Создайте файл `application.yml`:

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  application:
    name: wifi-vk-access-control
  datasource:
    url: jdbc:sqlite:/var/lib/wifi-vk-control/wifi_access.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.dialect.SQLiteDialect
    hibernate:
      ddl-auto: create-drop  # или 'validate' если БД уже создана

router:
  host: 192.168.1.1        # IP вашего роутера
  ssh-port: 22
  username: root
  password: your_router_password
  wifi-interface: wlan0    # Проверьте название интерфейса

proxy:
  port: 8888
  listen-address: 0.0.0.0
  capture-host: 0.0.0.0

vk-api:
  access-token: vk1.a.YOUR_TOKEN_HERE  # Замените на ваш токен
  group-id: 123456789                   # Замените на ID вашей группы
  api-version: 5.131
  base-url: https://api.vk.com/method

subscription:
  cache-ttl-minutes: 60
  check-on-new-device: true
  device-scan-interval-minutes: 5
  notification-message: |
    Привет! Для получения полного доступа в интернет, 
    пожалуйста, подпишись на нашу группу ВКонтакте.
    После подписки перезагрузи браузер.

logging:
  level:
    root: INFO
    com.wifi.access: DEBUG
  file:
    name: /opt/wifi-vk-control/logs/wifi-access.log
    max-size: 10MB
    max-history: 10
```

### 5. Развертывание

#### Способ 1: Локальное развертывание

```bash
# Скопируйте JAR на сервер
cp target/wifi-vk-access-control-1.0.0.jar /opt/wifi-vk-control/

# Скопируйте конфигурацию
cp application.yml /opt/wifi-vk-control/

# Перейдите в директорию
cd /opt/wifi-vk-control

# Запустите приложение
java -jar wifi-vk-access-control-1.0.0.jar --spring.config.location=./application.yml
```

#### Способ 2: Systemd сервис

**Создайте файл `/etc/systemd/system/wifi-vk-control.service`:**

```ini
[Unit]
Description=WiFi VK Access Control Service
After=network.target
Wants=network-online.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/opt/wifi-vk-control
ExecStart=/usr/bin/java -jar /opt/wifi-vk-control/wifi-vk-access-control-1.0.0.jar \
  --spring.config.location=/opt/wifi-vk-control/application.yml
Restart=on-failure
RestartSec=10

# Limits
LimitNOFILE=65535
LimitNPROC=65535

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=wifi-vk-control

[Install]
WantedBy=multi-user.target
```

**Запустите сервис:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable wifi-vk-control
sudo systemctl start wifi-vk-control
sudo systemctl status wifi-vk-control

# Просмотр логов
sudo journalctl -u wifi-vk-control -f
```

#### Способ 3: Docker контейнер

**Создайте `Dockerfile`:**

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /opt/wifi-vk-control

# Установка iptables (для локального тестирования)
RUN apt-get update && apt-get install -y iptables && rm -rf /var/lib/apt/lists/*

# Копирование JAR
COPY target/wifi-vk-access-control-1.0.0.jar .
COPY application.yml .

EXPOSE 8080 8888

CMD ["java", "-jar", "wifi-vk-access-control-1.0.0.jar", "--spring.config.location=./application.yml"]
```

**Сборка и запуск:**

```bash
docker build -t wifi-vk-control .
docker run -d \
  --name wifi-vk-control \
  -p 8080:8080 \
  -p 8888:8888 \
  -v /opt/wifi-vk-control/data:/data \
  -v /opt/wifi-vk-control/logs:/logs \
  wifi-vk-control
```

### 6. Настройка роутера

#### Создайте скрипт инициализации (опционально, приложение сделает это автоматически):

**Файл: `/etc/init.d/wifi-intercept`**

```bash
#!/bin/sh /etc/rc.common
# Simple traffic interception script

START=99

start() {
    echo "Starting WiFi traffic interception..."
    
    # Создание цепи правил
    iptables -t nat -N WIFI_CONTROL_wlan0 2>/dev/null || true
    
    # Редирект HTTP
    iptables -t nat -A WIFI_CONTROL_wlan0 -p tcp --dport 80 -j REDIRECT --to-port 8888
    
    # Редирект HTTPS
    iptables -t nat -A WIFI_CONTROL_wlan0 -p tcp --dport 443 -j REDIRECT --to-port 8888
    
    # Применение к входящему трафику
    iptables -t nat -A PREROUTING -i wlan0 -j WIFI_CONTROL_wlan0
    
    # Сохранение правил
    iptables-save > /etc/iptables.rules
    
    echo "Traffic interception started"
}

stop() {
    echo "Stopping WiFi traffic interception..."
    
    # Удаление правил
    iptables -t nat -D PREROUTING -i wlan0 -j WIFI_CONTROL_wlan0 2>/dev/null || true
    iptables -t nat -F WIFI_CONTROL_wlan0 2>/dev/null || true
    iptables -t nat -X WIFI_CONTROL_wlan0 2>/dev/null || true
    
    echo "Traffic interception stopped"
}
```

**Включение:**

```bash
chmod +x /etc/init.d/wifi-intercept
/etc/init.d/wifi-intercept enable
/etc/init.d/wifi-intercept start
```

### 7. Проверка установки

```bash
# Проверка запуска приложения
curl -X GET "http://localhost:8080/api/stats/summary"

# Должен вернуть что-то вроде:
# {"total_devices": 0, "subscribed_devices": 0, "unsubscribed_devices": 0}

# Проверка прокси сервера
curl -v http://localhost:8888/

# Проверка iptables правил
sudo iptables -t nat -L -n | grep WIFI_CONTROL

# Проверка логов
tail -f /opt/wifi-vk-control/logs/wifi-access.log
```

## Обновление приложения

```bash
# Остановка сервиса
sudo systemctl stop wifi-vk-control

# Сборка новой версии
mvn clean package

# Копирование нового JAR
cp target/wifi-vk-access-control-1.0.0.jar /opt/wifi-vk-control/

# Запуск сервиса
sudo systemctl start wifi-vk-control

# Проверка
sudo systemctl status wifi-vk-control
```

## Резервное копирование

```bash
# Резервное копирование БД
cp /var/lib/wifi-vk-control/wifi_access.db /var/backups/wifi_access.db.backup

# Резервное копирование логов
tar czf /var/backups/wifi-logs-$(date +%Y%m%d).tar.gz /opt/wifi-vk-control/logs/

# Автоматическое резервное копирование (cron)
# Добавить в crontab:
# 0 2 * * * cp /var/lib/wifi-vk-control/wifi_access.db /var/backups/wifi_access.db.backup
```

## Решение проблем при развертывании

### Проблема: "Permission denied" при подключении к роутеру
```
Решение:
1. Проверьте пароль в конфиге
2. Убедитесь, что SSH включен на роутере
3. Проверьте прав доступа: ssh root@192.168.1.1 (без пароля с ключами)
```

### Проблема: "Port 8080 is already in use"
```
Решение:
1. Измените порт в application.yml
2. Или освободите порт: lsof -i :8080 | kill -9 PID
3. Или используйте другой порт для приложения
```

### Проблема: "Database is locked"
```
Решение:
1. Проверьте, что нет других экземпляров приложения
2. Удалите lock файл: rm -f /var/lib/wifi-vk-control/*.db-lock
3. Перезагрузите приложение
```

### Проблема: VK API не работает
```
Решение:
1. Проверьте токен: curl "https://api.vk.com/method/groups.getById?group_ids=YOUR_GROUP_ID&access_token=YOUR_TOKEN&v=5.131"
2. Убедитесь, что группа открыта для API
3. Проверьте версию API (5.131 поддерживается)
4. Проверьте интернет-соединение
```

## Рекомендации по безопасности

1. **Измените пароль роутера** по умолчанию
2. **Используйте SSH ключи вместо паролей**
3. **Ограничьте доступ к порту 8080** (API)
4. **Используйте HTTPS** для API
5. **Регулярно обновляйте** зависимости и приложение
6. **Используйте VPN** для доступа к админ панели
7. **Включите файервол** на сервере приложения

## Мониторинг и обслуживание

```bash
# Проверка статуса сервиса
systemctl status wifi-vk-control

# Просмотр последних 100 строк логов
tail -100 /opt/wifi-vk-control/logs/wifi-access.log

# Мониторинг использования памяти
ps aux | grep java

# Проверка БД
sqlite3 /var/lib/wifi-vk-control/wifi_access.db
> SELECT COUNT(*) FROM users;
> SELECT COUNT(*) FROM access_logs;
> .exit

# Ротация логов (если не настроена автоматически)
# Добавить в crontab:
# 0 0 * * 0 cd /opt/wifi-vk-control/logs && \
#   gzip wifi-access.log && \
#   mv wifi-access.log.gz wifi-access.log.$(date +\%Y\%m\%d).gz
```

