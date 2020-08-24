<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<?php
    require "db.php";
    if($_SESSION['logged_used']==null || $_SESSION['device_select']==null){
      header('Location: /index');
      exit;
    }
?>

<head>
  <meta charset="UTF-8">
    <title>Расположение</title>
    <link rel="stylesheet" href="./style/menu.css">
    <link rel="stylesheet" href="./style/button.css">
    <link rel="stylesheet" href="./style/barsearch.css">
    <link rel="stylesheet" href="./style/osn.css">


    <script src="./libs/jquery-3.1.1.min.js"></script>
    <script src="./scripts/menu.js"></script>
    <script src="https://api-maps.yandex.ru/2.1/?lang=ru_RU" type="text/javascript"></script>
    
    <meta http-equiv="X-UA-Compatible" content=="IE=edge"/>
    <meta name="google" value="notranslate"/>
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

</head>
 <body>
  <section class="menu">
    <nav class="main-menu">
      <p></p>
      <div class="scrollbar" id="style-1">
        <ul>
          <li>                                   
            <a href="home">
              <i class="fa2 theme--light material-icons">home</i>
              <span class="nav-text">Главная</span>
            </a>
          </li>
          <li>                                   
            <a href="Contacts">
              <i class="fa2 theme--light material-icons">contacts</i>
              <span class="nav-text">Контакты</span>
            </a>
          </li>
          <li>                                 
            <a href="Call">
              <i class="fa2 theme--light material-icons">call</i>
              <span class="nav-text">Журнал вызовов</span>
            </a>
          </li>   
          <li>
            <a href="Block">
              <i class="fa2 theme--light material-icons">block</i>
              <span class="nav-text">Блок вызовов</span>
            </a>
          </li>
          <li>
            <a href="Sms">
              <i class="fa2 theme--light material-icons">message</i>
              <span class="nav-text">Сообщения</span>
            </a>
          </li>
          <li>
            <a href="FileManager">
              <i class="fa2 theme--light material-icons">storage</i>
              <span class="nav-text">Проводник</span>
            </a>
          </li>
          <li>
            <a href="Location">
              <i class="fa2 theme--light material-icons">my_location</i>
              <span class="nav-text">Расположение</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa2 theme--light material-icons">perm_media</i>
              <span class="nav-text">Загрузки</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa2 theme--light material-icons">view_list</i>
              <span class="nav-text">Команды</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa2 theme--light material-icons">list_alt</i>
              <span class="nav-text">Журнал приложений</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa2 theme--light material-icons">details</i>
              <span class="nav-text">Информация об устройстве</span>
            </a>
          </li>
          <li>
            <a href="#">
              <i class="fa2 theme--light material-icons">security</i>
              <span class="nav-text">Администратор устройства</span>
            </a>
          </li>
        </ul>
      </div>
    </nav>
    <header>
      <div class="head">
      <div class="main-info">
        Выбрано устройство: <?php echo $_SESSION['headerDev']; ?>
      </div>
      <div class="spacer"></div>

      <a href="app.apk" download="">
        <button type="button" class="btn" id="download">
          <span class="spn">
            <i class="theme--light material-icons md-36" >get_app</i>
          </span>
        </button>
      </a>
      <div class="usm" onclick="usermenu()">
        <nav title="User Menu" >
          <button type="button" class="btn">
            <span class="spn">
              <i class="theme--light material-icons md-36" >account_circle</i>
            </span>
          </button>
          <ul id="usermenu" class="UserMenu" data-open="false" >
            <li title="Индификатор">
              <span style="margin-left: 12px;" class="UserMenu_text">Индификатор: <?=$_SESSION['sid'] ?></span>
            </li>
            <li title="Профиль" class="UserMenu_brdt">
              <a href="Account">
                    <i class="fa2 theme--light material-icons wh"  >person</i>
                    <span class="UserMenu_text">Профиль</span>
                </a>
            </li>
            <li class="UserMenu_brdt" title="Выйти">
              <a href="logout">
                  <i class="fa2 theme--light material-icons wh">exit_to_app</i>
                  <span class="UserMenu_text">Выйти</span>
                </a>
            </li>
          </ul>
        </nav>
      </div>

      </div>
    </header>
  </section>

  <div class="v-content fix">
    <div class="v-content_wrap">
      <div class="container container--fluid">
        <div class="w3-container">
          <div class="w3-card-4">
            <header class="w3-container w3-blue w3-header"> <h1>Управление</h1> </header>

            <div class="status">
              <pre>Онлайн:<div id="lastonline"></div></pre>
              <pre>Лог:<div id="output"></div></pre> 
              <div id="log"></div>
            </div>

            <p></p>
            <input type = "button" id = "loaded" class="btn-load"  value = "Получить координаты" onclick="loaded();" style="margin-left:10px; width: 215px!important;"/>
            <pre></pre>
            <input type = "button" class="btn-del" value = "Отчистить" onclick="ochistka()" style="margin-left:10px;margin-bottom:10px;width: 215px!important;"/>
            
          </div>
        </div>
        <div id="list"></div>
        <div class="parent" style="margin-top: 40px;">
          <p></p>
          <div class="w3-container">
            <div class="w3-card-4 card">
              <p></p>
              <body>
                <div id="map" class="map" style="width: 100%; height: 400px"></div>
              </body>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
  
</body>

<script type="text/javascript">
  var myList;

  function setLocationMap(numOne,numTwo){
    ochistka();
    ymaps.ready(function () {
      var map = new ymaps.Map("map", {
        center: [numOne,numTwo],
        zoom: 15
      });
      
      var myPlacemark = new ymaps.Placemark([numOne,numTwo]);
      
      map.geoObjects.add(myPlacemark);
      
      circle = new ymaps.Circle([[numOne,numTwo], 410], null, { draggable: false });
      map.geoObjects.add(circle);
      
      map.setType('yandex#map');
    });
  }
  
  getOnline();

  getOutput(); 

  getLoc();
  
  function ochistka(){
     $("ymaps").remove();
  }

  function loaded(){
    $.post( 
      "/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php",
      { cmd: "",spec:"getLocation"},
      function(data) {
        $('#stage').html(data);
      }
    );

    //window.location.reload(true);
  }

	function getOnline() {
	   
	    $('#lastonline').load('/Devices/<?=$_SESSION['device_select']->device_id ?>/getlastonline.php');
	    setTimeout("getOnline()",2000);
	}

	function getOutput() {
    
	    $('#log').load('/Devices/<?=$_SESSION['device_select']->device_id ?>/show_output.php').hide(true);
	    var stri = $('#log').text();
	    if(stri.includes("LC"))
	    	$('#output').html($('#log').text());
	    setTimeout("getOutput()",2000);
	}
	function getLoc(){
    var JsonList=$.getJSON("/Devices/<?=$_SESSION['device_select']->device_id ?>/info/Location/loc.json",function(data){
    	myList = data;
      setLocationMap(myList['lat'],myList['lng']);
      
    });
    
    setTimeout("getLoc()",20000);
	    
	}
  
  
</script>

</html>