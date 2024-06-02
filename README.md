# Автоматическое закрепление SmartMatch
_ref: [Техническое задание](https://docs.google.com/document/d/1Oc6vNmB1S7iFU8d62P16DfhGp1Ja9PEedhL3NxFU2p8/edit)_

### Общее
Процесс автозакрепления запускается ежедневно в 21:00.

Также процесс можно запустить вручную, отправив `GET` запрос по адресу `/autoassign/start`.

Чтобы менеджер получил письмо, необходимо, чтобы его email был в списке разрешенных, в поле `app.email.whitelist` файла `application.properties`

### Запуск
Запустить процесс имеется несколько возможностей.

#### I. Установка IDE
1. Установить БД PostgreSQL, создать юзера `data` с паролем `data`, создать базу данных `datadb`, создать схему `smartmatch`..

    1.1. Инструкция по установке PostgreSQL - [тык](https://docs.rkeeper.ru/rk7/7.7.0/ru/ustanovka-postgresql-na-windows-29421153.html). Установить лучше всего все компоненты, особенно pgAdmin4 (GUI-tool для работы с БД).
    
    1.2. Запустить pgAdmin4, залогиниться по реквизитам, указанным при установке. Создать юзеров с БД можно через графические инструменты или команды языка, ниже инструкция для _команд_.

    1.3. В списке Database сделать ПКМ на любую, открыть Query Tool.
        Исполнить следующие команды:
```SQL
create database datadb; --обязательно выполнить отдельно от остальных комманд
       
create user data password 'data';   --можно вместе выполнить
grant all on database datadb to data;
```
Перед созданием схемы надо обновить список Database, и переключиться на Query Tool созданной БД datadb (также через ПКМ) и выполнить.
```SQL
create schema "smartmatch";     --создать схему и выдать права юзеру
grant all on schema "smartmatch" to data;
```

2. Скачать Intellij IDEA, запустить.
3. Выбрать `File` -> `New`-> `Project from Version Control...`. В URL вставить https://github.com/rreshrr/SmartMatch.git, выбрать директорию -> `OK`.
4. Отыскать в дереве файлов файл `src/main/resources/application.properties`. В нём есть строка
`app.path-to-csv`, в ней указан путь до папки, хранящей файлики-заглушки (`dwh_clients.csv`, `mdm_managers.csv`, `sap_managers.csv`).
Нужно указать путь для своего компьютера до этих файлов (лежат там же, где и `application.properties`) ИЛИ указать любую другую папку, в которой есть файлы с такими же названиями. Это наши входные данные.

    Можно сделать это быстро: кликнуть ПКМ на папку `resources`, выбрать `Copy Path/Reference...`->`Absolute Path`. **Сохранить!**

    Если сидите на Windows, то обязательно экранируйте слеши, должно получиться что-то вроде: `C:\\Users\\Lala\\Dada\\src\\...`
5. Отыскать в дереве файлов главный java-класс: `src/main/java/ru/alfastudents/smartmatch/SmartMatchApplication.java`
6. Запустить приложение `Run` -> `Run 'SmartMatchApplication'`.
7. Дождаться 21:00... Или открыть браузер, и перейти по адресу http://localhost:8080/autoassign/start - переход по ссылке запустит процесс.
8. Если появилась надпись "AutoAssignProcess finished", то всё ок. Можно идти смотреть через pgAdmin на таблицу `AutoAssignCase` внутри схемы `smartmatch`. Внутри таблицы должны лежать все закрепления, на основе данных из файлов-заглушек.    

#### II. Через Docker
 //to-do
