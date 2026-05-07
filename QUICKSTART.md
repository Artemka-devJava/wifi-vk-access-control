# QUICKSTART.md - Быстрый старт

## За 5 минут к работающей системе

### Шаг 1: Подготовка (2 минуты)

```bash
# Клонируйте репозиторий
git clone <repository-url>
cd wifi-vk-access-control

# Установите Java 11+
# На Ubuntu/Debian:
sudo apt-get install openjdk-11-jre-headless
# На Windows: скачайте с https://jdk.java.net/11
```

### Шаг 2: Получите VK API токен (2 минуты)

1. **Создайте группу в ВКонтакте** (если еще нет)
   - vk.com → Сообщества → Создать → Сообщество (выберите "группа")

2. **Получите API токен**
   - Зайдите в группу → Управление → Разработчикам → API
   - Нажмите "Создать токен"
   - Выберите разрешения: `messages`, `groups`
   - Скопируйте токен (начинается на `vk1.a.`)

3. **Запомните ID группы**
   - Из URL группы: `vk.com/club123456789` → ID = `123456789`

### Шаг 3: Конфигурация (1 минута)

**Создайте файл `application.yml` в корне проекта:**

```yaml
server:
  port: 8080
  servlet:
    context-path: /api

router:
  host: 192.168.1.1
  ssh-port: 22
  username: root
  password: admin123

proxy:
  port: 8888
  listen-address: 0.0.0.0

vk-api:
  access-token: vk1.a.YOUR_TOKEN_HERE
  group-id: YOUR_GROUP_ID
  api-version: 5.131
  base-url: https://api.vk.com/method

subscription:
  cache-ttl-minutes: 60
  notification-message: |
    Привет! Для полного доступа в интернет подпишись на нашу группу!
```

### Шаг 4: Запуск (1 минута)

**Для разработки:**
```bash
# Сборка
mvn clean package

# Запуск
java -jar target/wifi-vk-access-control-1.0.0.jar \
  --spring.config.location=./application.yml
```

**Приложение запустится на http://localhost:8080**

## Тестирование

### Проверьте, что приложение работает:

```bash
# Получить статистику
curl -X GET "http://localhost:8080/api/stats/summary"

# Должен вернуть:
# {"total_devices":0,"subscribed_devices":0,"unsubscribed_devices":0}
```

### Протестируйте API:

```bash
# Проверить доступ (устройства еще нет в БД)
curl -X GET "http://localhost:8080/api/auth/check-access?macAddress=AA:BB:CC:DD:EE:FF&ipAddress=192.168.1.100&url=http://example.com"

# Связать устройство с VK пользователем
# (замените 123456789 на ваш VK ID)
curl -X POST "http://localhost:8080/api/auth/link-device?macAddress=AA:BB:CC:DD:EE:FF&vkUserId=123456789"

# Получить информацию об устройстве
curl -X GET "http://localhost:8080/api/auth/user?macAddress=AA:BB:CC:DD:EE:FF"
```

## Что дальше?

### Для разработки:
- Изучите `README.md` для полной документации
- Смотрите `EXAMPLES.md` для примеров API
- Изучите код в `src/main/java/com/wifi/access/`

### Для развертывания:
- Следуйте `DEPLOYMENT.md` для production среды
- Настройте SSH доступ к роутеру
- Создайте systemd сервис
- Настройте firewall правила

### Для интеграции с роутером:
- Убедитесь что SSH включен на роутере
- Установите iptables на роутере
- Приложение автоматически настроит правила при запуске

## Решение проблем

### "Connection refused" при запуске?
```bash
# Проверьте, что порт 8080 свободен
lsof -i :8080

# Если занят, измените в application.yml:
server:
  port: 9090  # вместо 8080
```

### "Cannot resolve host" в VK API?
```bash
# Проверьте интернет-соединение
ping api.vk.com

# Проверьте VK токен и ID группы
curl "https://api.vk.com/method/groups.getById?group_ids=YOUR_GROUP_ID&access_token=YOUR_TOKEN&v=5.131"
```

### Роутер недоступен?
```bash
# Проверьте IP адрес роутера
ping 192.168.1.1

# Проверьте SSH доступ
ssh root@192.168.1.1
```

## Структура проекта

```
wifi-vk-access-control/
├── src/main/java/com/wifi/access/
│   ├── config/           # Конфигурация
│   ├── controller/        # REST API (новое)
│   ├── dto/              # Модели данных
│   ├── entity/           # JPA сущности БД
│   ├── service/          # Бизнес-логика
│   ├── vk/               # VK API интеграция
│   ├── router/           # Управление роутером
│   └── proxy/            # HTTP прокси сервер
├── src/main/resources/
│   └── application.yml   # Конфигурация приложения
├── README.md             # Полная документация
├── EXAMPLES.md           # Примеры использования
├── DEPLOYMENT.md         # Руководство развертывания
└── pom.xml              # Maven конфигурация
```

## REST API (основное)

| Метод | Путь | Описание |
|-------|------|---------|
| GET | `/api/auth/check-access` | Проверить доступ |
| POST | `/api/auth/link-device` | Связать устройство с VK |
| GET | `/api/auth/user` | Получить информацию об устройстве |
| GET | `/api/stats/summary` | Получить статистику |
| GET | `/api/stats/devices` | Список всех устройств |
| GET | `/api/stats/access-logs` | Логи доступа устройства |

## Основные компоненты

- **ProxyServer** - HTTP прокси (Netty) на порту 8888
- **AccessController** - Проверка доступа и подписки
- **VkSubscriptionChecker** - Проверка подписки в ВКонтакте с кешем
- **VkNotificationSender** - Отправка сообщений в ВКонтакте
- **RouterConnectionManager** - SSH управление роутером
- **DeviceManager** - Управление устройствами (MAC адреса)

## Конфигурация по умолчанию

| Параметр | Значение | Назначение |
|----------|----------|-----------|
| API порт | 8080 | REST API приложения |
| Прокси порт | 8888 | HTTP прокси для перехвата |
| Кеш подписки | 60 минут | Кеширование проверок VK API |
| Сканирование | 5 минут | Периодическое сканирование устройств |

## Логирование

Логи записываются в консоль и файл (если настроен):

```bash
# Для просмотра логов в реал-времени:
tail -f logs/wifi-access.log
```

Уровни логирования:
- `INFO` - основные события
- `DEBUG` - детальная информация (приложение)
- `WARN` - предупреждения
- `ERROR` - ошибки

## Следующие шаги

1. ✅ Запустил приложение локально
2. → **Следующее:** Настроить SSH доступ к роутеру
3. → Настроить правила iptables на роутере
4. → Развернуть на production сервер
5. → Настроить systemd сервис

---

**Нужна помощь?**
- Смотрите `README.md` для полной документации
- Смотрите `EXAMPLES.md` для примеров API
- Смотрите `DEPLOYMENT.md` для развертывания
- Смотрите логи для диагностики проблем

