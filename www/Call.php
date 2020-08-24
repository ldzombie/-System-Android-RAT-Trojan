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
  <title>Журнал Вызовов</title>
  <link rel="stylesheet" href="./style/callLog.css">
  <link rel="stylesheet" href="./style/menu.css">
  <link rel="stylesheet" href="./style/osn.css">
  <link rel="stylesheet" href="./style/barsearch.css">
  <link rel="stylesheet" href="./style/button.css">

  <script src="./libs/jquery-3.1.1.min.js"></script>
  <script src="./scripts/menu.js"></script>

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

      <div id="ChangeBoard" class="changeBoard"  >
        <form action="/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php" method="post" name="changeF" autocomplete="off">
          <div id="numOrig" style="visibility:hidden;font-size: 0.001em;"></div>
          <input type="text" name="date" id="date" value="" />
          <input type="text" name="phonenum" id="phonenum" value="" />
          <input type="text" name="Cduration" id="Cduration" value="" />
          <select id="chntype" name="type" size="1">
            <option value="1">Входящий Звонок</option>
            <option value="2">Выходящий Звонок</option>
            <option value="3">Пропущенный</option>
          </select>
        </form>
        <input type="button" id="chnb" value="Изменить" />
      </div>

      <div class="container container--fluid">
        <div class="w3-container">
          <div class="w3-card-4">
            <header class="w3-container w3-blue w3-header"> <h1>Управление</h1> </header>

            <div class="status">
              <pre>Онлайн:<div id="lastonline"></div></pre>
              <pre>Лог:<div id="output"></div></pre> 
              <div id="log"></div>
            </div>

            <div class="addBoard">
              <div style="float:left;">
                <input type="text" placeholder="Дата" id="add_date" autocomplete="off" value=""/>
                <input type="text" placeholder="Номер" id="addphone" autocomplete="off" value="" />
                <input type="text" placeholder="Продолжительность" autocomplete="off" id="add_duration" value="" />
                
              </div>
              <div >
                <input type="button" class="btn-add" id="btnAdd" value="Добавить" />
                <select id="addtype" name="type" size="1">
                  <option selected="selected" value="1">Входящий Звонок</option>
                  <option  value="2">Выходящий Звонок</option>
                  <option value="3">Пропущенный</option>
                </select>
                <input type="text" placeholder="Количество" autocomplete="off" id="add_count" value="1" />
              </div>
              
            </div>

            <p></p>
            <input type = "button" id = "loaded" class="btn-load" value = "Загрузить" onclick="loaded()" style="margin-left:10px;"/>
            <pre></pre>
            <input type = "button" id = "clearSms" class="btn-del" value = "Отчистить" onclick="ochistka()" style="margin-left:10px;margin-bottom:10px;"/>
            
          </div>
        </div>
        <div class="barsearch" >
            <input type="text" id="search" placeholder="Поиск по таблице" autocomplete="off">
        </div>
        <div class="parent" style="margin-top: 40px;">
          <p></p>
          <div class="w3-container">
            <div class="w3-card-4 card">
              <p></p>
              <body>
                <table id="excelDataTable" border="1">
                </table>
              </body>
            </div>
          </div>
        </div>

      </div>
    </div>
  </div>
  
</body>

<script>
$(document).ready(function(){
  $("#search").keyup(function(){
    _this = this;
  $.each($("#excelDataTable tbody tr"), function() {
          if($(this).text().toLowerCase().indexOf($(_this).val().toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("phoneNumber".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("type".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("Controls".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("duration".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("date".toLowerCase()) === -1){
              $(this).hide();
          } else {
              $(this).show();                
          }
        });
    })
  });

</script>

<script type="text/javascript">
	
  var myList;

  getOnline();

  getOutput(); 

  getTab();

  function buildHtmlTable(selector) {
    ochistka();
    var columns = addAllColumnHeaders(myList, selector);

    for (var i = 0; i < myList.length; i++) {
      var row$ = $('<tr/>');
      for (var colIndex = 0; colIndex < columns.length; colIndex++) {
        var cellValue = myList[i][columns[colIndex]];
        if (cellValue == null) cellValue = "";

        row$.append($('<td/>').html(cellValue));
      }
      row$.append($('<td/>').html("<input type=button value=\"Удалить\" style=\"width:50%;heigth:100%;padding:0;float:left;\" class=\"btn-del\" onclick=\"del('"+i+"')\"> <input type=button value=\"Изменить\" style=\"width:50%;heigth:100%;padding:0;float:right;\" class=\"btn-chn\" onclick=\"chn('"+i+"')\">"));
      $(selector).append(row$);
    }
  }

  function addAllColumnHeaders(myList, selector) {
    var columnSet = [];
    var headerTr$ = $('<tr/>');

    for (var i = 0; i < myList.length; i++) {
      var rowHash = myList[i];
      for (var key in rowHash) {
          
        if ($.inArray(key, columnSet) == -1 && key != "dateN") {
          columnSet.push(key);

          headerTr$.append($('<th/>').html(key));
        }
      }
    }
    headerTr$.append($('<th/>').html("Controls"));
    $(selector).append(headerTr$);

    return columnSet;
  }

  function ochistka(){
     $("tr").remove();
  }

  function del(num){
    var i= num[0];
    var dateN = myList[i]["dateN"];

    let call = {
          date:dateN,

          toString() {
            return `{ date: "${this.date}"}`;
          }
        };
    $.post( 
      "/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php",
      { cmd: "",spec:"CallDel "+call},
      function(data) {
        $('#stage').html(data);
      }
    );

  }

  function loaded(){
    $.post( 
      "/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php",
      { cmd: "",spec:"getCallLog"},
      function(data) {
        $('#stage').html(data);
        
      }
    );
  }

  function chn(num){
    var i= num[0];
    var elem = document.querySelector("#ChangeBoard");
    elem.classList.toggle("open");
    
    var date = myList[i]["date"];
    var phone = myList[i]["phoneNumber"];
    var type = myList[i]["type"];
    var dur = myList[i]["duration"];
    var t;

    if(type=="-->(out)")
      t=2;
    else if(type=="<--(in)")
      t=1;
    else
      t=3;
    
    document.changeF.date.value = date;
    document.changeF.phonenum.value = phone;
    document.changeF.Cduration.value = dur;
    document.changeF.chntype.selectedIndex = t-1;
    $("#numOrig").html(i);
  }
  

  function getOnline(){
    $('#lastonline').load('/Devices/<?=$_SESSION['device_select']->device_id ?>/getlastonline.php');
    setTimeout("getOnline()",2000);
  }

  function getOutput(){
    $('#log').load('/Devices/<?=$_SESSION['device_select']->device_id ?>/show_output.php').hide(true);
    var str = $('#log').text();
    if(str.includes("Call"))
    	$('#output').html($('#log').text());

    setTimeout("getOutput()",2000);
  }

  function getTab(){
    var JsonList=$.getJSON("/Devices/<?=$_SESSION['device_select']->device_id ?>/info/Calls/callLog.json",function(data){
      myList = data['callsList'];
      buildHtmlTable('#excelDataTable');
    });
    
    setTimeout("getTab()",20000);
  }
  
</script>
<script type = "text/javascript" language = "javascript">
$(document).ready(function() {
    $("#chnb").click(function(event){
      var num = $("#numOrig").html();

      var dateV = myList[num]["date"];
      var phoneV = myList[num]["phoneNumber"];

      var N_date =$("#date").val();
      var N_phone = $("#phonenum").val();
      var N_dur = $("#Cduration").val();

      var select = document.getElementById("chntype");
      var value = select.value;

      var N_type = value;

      let call = {
          phone:phoneV,
          date: dateV,
          Ntype: N_type,
          Nphone:N_phone,
          Ndur:N_dur,
          Ndate:N_date,

          toString() {
            return `{date: "${this.date}", phone: "${this.phone}",Ntype: "${this.Ntype}",Nphone: "${this.Nphone}",Ndur: "${this.Ndur}",Ndate: "${this.Ndate}"}`;
          }
        };

      $.post( 
        "/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php",
        { cmd: "",spec:"ContChn "+call},
        function(data) {
          $('#stage').html(data);
        }
      );
      
      var elem = document.querySelector("#ChangeBoard");
      elem.classList.toggle("open");

      document.changeF.date.value = "";
      document.changeF.phonenum.value = "";
      document.changeF.Cduration.value = "";
      $("#numOrig").html("");
    });

    $("#btnAdd").click(function(event){
        var phoneV = $("#addphone").val();
        var dateV = $("#add_date").val();

        var select = document.getElementById("addtype");
        var value = select.value;

        var typeV = value;
        var durationV = $("#add_duration").val();
        var countV = $("#add_count").val();

        if(countV == "")
        	countV=1;

        let call = {
          date: dateV,
          phone: phoneV,
          type:typeV,
          duration:durationV,
          count:countV,

          toString() {
            return `{date: "${this.date}", phone: "${this.phone}", type: "${this.type}", duration: "${this.duration}" , count: "${this.count}" }`;
          }
        };

        $.post( 
          "/Devices/<?=$_SESSION['device_select']->device_id ?>/cmd.php",
          { cmd: "",spec:"CallAdd "+call},
          function(data) {
            $('#stage').html(data);
          }
        );
        $("#addphone").val("");
        $("#add_date").val("");
        $("#add_duration").val("");
    });
  });
</script>


</html>