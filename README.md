# Автоматическое закрепление SmartMatch
_ref: [Техническое задание](https://docs.google.com/document/d/1Oc6vNmB1S7iFU8d62P16DfhGp1Ja9PEedhL3NxFU2p8/edit)_

<!-- TOC -->
* [Автоматическое закрепление SmartMatch](#автоматическое-закрепление-smartmatch)
* [Общее](#общее)
* [Запуск](#запуск)
  * [I. Через клон проекта (Intellij IDEA, PostgreSQL)](#i-через-клон-проекта-intellij-idea-postgresql)
  * [II. Через docker compose (рекомендуемый!)](#ii-через-docker-compose-рекомендуемый)
<!-- TOC -->

# Общее
Процесс автозакрепления запускается ежедневно в 21:00.

Также процесс можно запустить вручную, отправив `GET` запрос по адресу `/autoassign/start`.

Чтобы менеджер получил письмо, необходимо, чтобы его email был в списке разрешенных, в поле `app.email.whitelist` файла `application.properties`

Все интеграции со внешними системами реализованы с помощью файлов-заглушек.

Обработка клиентов выполняется параллельно.
# Запуск
Есть несколько способов запустить процесс.

## I. Через клон проекта (Intellij IDEA, PostgreSQL)
1. Установить БД PostgreSQL, создать мастер-юзера.

&emsp;&emsp;1.1. Инструкция по установке PostgreSQL - [тык](https://docs.rkeeper.ru/rk7/7.7.0/ru/ustanovka-postgresql-na-windows-29421153.html). Установить лучше всего все компоненты, особенно pgAdmin4 (GUI-tool для работы с БД).

&emsp;&emsp;1.2. Запомнить реквизиты, ещё лучше - оставить по умолчанию.

2. Скачать Intellij IDEA, запустить.
3. Выбрать `File` -> `New`-> `Project from Version Control...`. В URL вставить https://github.com/rreshrr/SmartMatch.git, выбрать директорию -> `OK`.
4. Отыскать в дереве файлов файл с настройками `src/main/resources/application.properties` и поправить ряд настроек.

&emsp;&emsp;4.1. Настройка
   `app.path-to-csv`. В ней указан путь до папки, хранящей файлики-заглушки (`dwh_clients.csv`, `mdm_managers.csv`, `sap_managers.csv`).
   Нужно указать путь для своего компьютера до этих файлов (лежат там же, где и `application.properties`) ИЛИ указать любую другую папку, в которой есть файлы с такими же названиями. Это наши входные данные.

&emsp;&emsp;Можно сделать это быстро: кликнуть ПКМ на папку `resources`, выбрать `Copy Path/Reference...`->`Absolute Path` и вставить полученное значение. **Сохранить!**

&emsp;&emsp;Если сидите на Windows, то обязательно экранируйте слеши, должно получиться что-то вроде: `C:\\Users\\Lala\\Dada\\src\\...`

&emsp;&emsp;4.2. В настройке `spring.mail.password` заменить пароль от почтового ящика на актуальный (спросить у меня).

&emsp;&emsp;4.3. В настройках `spring.datasource.username` и `spring.datasource.password` установить значения логина и пароля мастер-юзера БД. (если оставляли по умолчанию, то можно не менять).
5. Отыскать в дереве файлов главный java-класс: `src/main/java/ru/alfastudents/smartmatch/SmartMatchApplication.java`
6. Запустить приложение `Run` -> `Run 'SmartMatchApplication'`.
7. Дождаться 21:00... Или открыть браузер, и перейти по адресу http://localhost:8080/autoassign/start - переход по ссылке запустит процесс.
8. Если появилась надпись "AutoAssignProcess finished", то всё ок. Можно идти смотреть через pgAdmin на таблицу `autoassigncases` внутри схемы `smartmatch`. Внутри таблицы должны лежать все закрепления, на основе данных из файлов-заглушек.    

## II. Через docker compose (рекомендуемый!)
 1. Скачать и установить Docker Desktop (инструкция - [тык](https://docs.docker.com/desktop/install/windows-install/))
2. Создать на пк тестовую папку, в неё поместить файл `docker-compose.yaml` (скачать из репозитория) + создать папку, где будут лежать файлы-заглушки (`dwh_clients.csv`, `mdm_managers.csv`, `sap_managers.csv`, их можно скачать из репозитория `src/main/resources/`)
3. Поправить docker-compose.yaml:

&emsp;&emsp;3.1. Изменить свойство `volumes`:
```yaml
volumes:
    - /путь/до/папки/c/заглушками:/app/external-system-data #для linux/mac
    
    - C:\\путь\\до\\папки\\с\\заглушками:/app/external-system-data #для windows
```
&emsp;&emsp;3.2. В свойстве APP_EMAIL_PASSWORD указать актуальный пароль (спросить у меня)

```yaml
    - APP_EMAIL_PASSWORD=secretpass
```

&emsp;&emsp;3.3. В свойстве APP_EMAIL_WHITE_LIST можно добавить почтовые ящики, которым разрешено отправлять письма.

4. Запустить контейнер

&emsp;&emsp;4.1. Открыть терминал/командную строку в папке с `docker-compose.yaml`

&emsp;&emsp;4.2. Выполнить команду
```shell
docker compose up
```
&emsp;&emsp;4.3. Дождаться пока все скачается, запуститься, инициализируется...
Финальной строкой должно быть что-то вроде:
```shell
smartmatch  | ... : Completed initialization in 2ms
```
5. Запустить сам процесс - перейти в браузере по ссылке http://localhost:8085/autoassign/start. Если вывелось "AutoAssignProcess finished", то все ок.

Полезные ссылочки: 
- http://localhost:8085/autoassign - получить JSON со всеми закреплениями из локальной БД.
- http://localhost:8085/autoassign/clear - очистить локальную БД с результатами закреплений.

Каждый запуск http://localhost:8085/autoassign/start берет данные из ваших файлов и пытается выполнить закрепление.

Каждый запуск контейнера создает с 0 БД, поэтому данные не сохранятся.

