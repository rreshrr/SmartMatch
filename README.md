# Автоматическое закрепление SmartMatch
_ref: [Техническое задание](https://docs.google.com/document/d/1Oc6vNmB1S7iFU8d62P16DfhGp1Ja9PEedhL3NxFU2p8/edit)_

### Запуск
Процесс автозакрепления запускается ежедневно в 21:00.

Также процесс можно запустить вручную, отправив `GET` запрос по адресу `/autoassign/start`.

### Отправка писем
Чтобы менеджер получил письмо, необходимо, чтобы его email был в списке разрешенных, в поле `app.email.whitelist` файла `application.properties`

### Тестирование
