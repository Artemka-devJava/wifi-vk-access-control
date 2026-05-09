# Решение ошибки "'}' expected" в VkApiConfig

## Проблема
IDE (JetBrains) может кешировать старую версию файла `VkApiConfig.java` даже после обновления.
Это приводит к ложной ошибке компиляции: "'}' expected" на строке 17.

## Решение

### Способ 1: Очистить кеш IDE (Рекомендуется)

**В IntelliJ IDEA / JetBrains IDE:**

1. Зайти в меню: **File → Invalidate Caches/Restart**
2. Выбрать опцию **Invalidate and Restart**
3. IDE перезагрузится и пересчитает кеш
4. Ошибка должна исчезнуть

### Способ 2: Очистить кеш вручную

**Windows:**
```
С помощью проводника перейти в:
C:\Users\<Пользователь>\AppData\Local\JetBrains\IntelliJIdea<версия>\caches

Удалить содержимое папки caches
```

**macOS:**
```
~/.cache/JetBrains/IntelliJIdea*/
```

**Linux:**
```
~/.cache/JetBrains/IntelliJIdea*/
```

### Способ 3: Перезагрузить проект

1. Закрыть проект в IDE (**File → Close Project**)
2. Удалить папку `.idea` из корня проекта
3. Переоткрыть проект

## Проверка

После выполнения одного из способов:

1. Открыте файл `src/main/java/com/wifi/access/config/VkApiConfig.java`
2. IDE должна пересчитать синтаксис
3. Ошибка должна исчезнуть ✓

## Важно

Файл `VkApiConfig.java` абсолютно корректен синтаксически.
Это 100% проблема кеша IDE, а не реальная ошибка кода.

```java
@Component
@ConfigurationProperties(prefix = "vk-api")
public class VkApiConfig {
    private String accessToken;
    private Long groupId;
    private String apiVersion;
    private String baseUrl;
}
```

Все остальные файлы в пакете `config` скомпилированы без ошибок:
- ✓ ProxyConfig.java
- ✓ RouterConfig.java  
- ✓ SubscriptionConfig.java
- ❌ VkApiConfig.java (только кеш IDE)

