<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
<?php
    require "db.php";
    if($_SESSION['logged_used']==null){
      header('Location: /index');
      exit;
    }

	$data = $_POST;
	$sid=1;
	$scount=0;
	if(isset($data['do_add'])){
		$errors = array();
		if(trim($data['login'])=='' ){
			$errors[] = "Введите логин";
		}
		if($data['password']=='' ){
			$errors[] = "Введите пароль";
		}
		if($data['password_2']!=$data['password'] ){
			$errors[] = "Пароли не совпадают";
		}
		if(R::count('users',"login=?",array($data['login'])) > 0){
			$errors[] = "Пользователь с таким логином и идентификатором уже существует";
		}
		if($data['indification'] == ''){
			$scount=R::count('users',"sid=?",array($sid));
			while($scount>0){
				$sid++;
				$scount=R::count('users',"sid=?",array($sid));
			}
		}
		if($data['indification'] != ''){
			$sid = $data['indification'];
			if($sid > 1){
				$scount=R::count('users',"sid=?",array($sid));
				while($scount>0){
					$sid++;
					$scount=R::count('users',"sid=?",array($sid));
				}
			}
		}
		
		if(empty($errors)){
			$user = R::dispense('users');
			$user->admin=false;
			$user->login = $data['login'];
			$user->password = password_hash($data['password'], PASSWORD_DEFAULT);
			$user->sid = $sid;
			$user->devices=0;
			R::store($user);

		}
	}

?>

<head>
  <meta charset="UTF-8">
  <title>Профиль</title>
  <link rel="stylesheet" href="./style/account.css">
  <link rel="stylesheet" href="./style/menu.css">
  <link rel="stylesheet" href="./style/button.css">
  <link rel="stylesheet" href="./style/barsearch.css">
  <link rel="stylesheet" href="./style/osn.css">


  <script src="./libs/jquery-3.1.1.min.js"></script>
  <script src="./scripts/menu.js"></script>
  <script src="./scripts/account.js"></script>

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

      <div id="ChangeBoard" class="changeBoard" >
        <form action="#" method="post" name="changeF" autocomplete="off">
          <input type="text" name="chn_login" placeholder="Логин"  id="chn_name" value="" />
          <input type="text" name="chn_sid" placeholder="Индификатор"  id="chn_sid" value="" />
          <input type="text" name="chn_password" placeholder="Пароль"  id="chn_password" value="" />
          <input type="button" id="chnb" value="Изменить" />
        </form>
      </div>

      
      <div class="container container--fluid">
      	<container class="main">
            <section class="left-panel">
              <div class="avatar-panel">
                <h4><div class="main-name"><?=$_SESSION['logged_used']->login ?></div></h4>
                <h6>Индификатор: <?=$_SESSION['logged_used']->sid ?></h6>
              </div>
              <ul>
              	<?php if($_SESSION['admin'] == true) { echo '
              	<li>
	                  <button class="tablinks" onclick="openPage(\'dashboard\',\'0\')">
	                    <i class="fa2 material-icons md-35">dashboard</i>
	                    <span>Информация</span>
	                  </button>
	                </li>
	                <li>
	                  <button class="tablinks" onclick="openPage(\'accounts\',\'1\')">
	                    <i class="fa2 material-icons md-35">contacts</i>

	                    <span>Менаджер Аккаунтов</span>
	                  </button>
	                </li> ';
	                 } ?>
                <li>
                  <button class="tablinks" onclick="openPage('change_panel','2')">
                     <i class="fa2 material-icons md-35">settings</i>
                    <span>Настройки</span>
                  </button>
                </li>
              </ul>
          </section>
          <section class="content">
            
             <div id="accounts" class="tabcontent">
                <div class="row first-row">
                  <div class="menu">
                    <i class="fa fa-bars" aria-hidden="true"></i>
                  </div>
                  <div class="page-header">Менаджер Аккаунтов</div>
                </div>
               
               <div class="row">
                    <div class="box box-1">
                      <div class="box-header">
                        <a class="box-title" href="#">
                          <i class="fa2 material-icons md-35 wd">bar_chart</i>
                            <span>Добавить</span>
                        </a>  
                      </div>
                      
                      <div class="boxes-container">
                        <form  action="#" method="POST" class="add-container" autocomplete="off">
                          <input type="text" placeholder="Логин" name="login" id="login" value="" />
                          <input type="text" placeholder="Индификатор" name="indification" id="indification" value="" />
                          <input type="password" placeholder="Пароль"  name="password" id="password" value="" />
                          <input type="password" placeholder="Пароль"  name="password_2" id="password_2" value="" />
                          <button type="submit" name="do_add">Добавить</button>
                        </form>
                        
                      </div>
                    </div>
                </div>
               
              	<div class="row">
                    <div class="box box-1">
                      <div class="box-header">
                        <a class="box-title" href="#">
                          <i class="fa2 material-icons md-35 wd">bar_chart</i>
                            <span>Аккаунты</span>
                        </a>  
                      </div>
                      
                      <div class="boxes-container">
                        <div class="users-list">
                          
                          <table class="table-users" id="tusers" width="100%">
                            <thead>
                                <th>Login</th>
                                <th>User ID</th> 
                                <th>Devices</th>
                                <th></th>
                                <th></th>
                             </thead>
                             <tbody>
                              <?php
                                $users = R::findAll('users');
                                foreach ($users as $item ) { ?>
                                  <tr>
                                    <td><?=$item->login ?></td>
                                    <td><?=$item->sid ?></td> 
                                    <td><?=$item->devices ?></td>
                                    
                                    <td style="width:100px;"><a href="#" onclick="chn(<?=$item->login ?>,<?=$item->sid ?>)" class="btn-chn" style="width: 100px; height: 100%; text-align: center;" >Изменить</a></td>
                                    <td style="width:100px;"><a href="#" onclick="del(<?=$item->login ?>,<?=$item->sid ?>)" class="btn-del" style="width: 100px; height: 100%;" >Удалить</a></td>
                                  </tr>
                              <?php  } ?>
                            </tbody>
                          </table>
                          <?php if($users==null) { echo '<div style="color: red; font-size: 22px;">
                            <i class="theme--light material-icons" style="color: red;" >info</i> Пользователи отсутствуют
                          </div>';
                          
                          } ?>
                        </div>
                      </div>
                    </div>
                </div>
               
               
            	</div>
            
            <div id="dashboard" class="tabcontent" >
                <div class="row first-row">
                  <div class="menu">
                    <i class="fa fa-bars" aria-hidden="true"></i>
                  </div>
                  <div class="page-header">Информация</div>
                </div>

              <div class="row">
                    <div class="box box-1">
                      <div class="box-header">
                        <a class="box-title" href="#">
                          <i class="fa2 material-icons md-35 wd">bar_chart</i>
                            <span>Информация</span>
                        </a>  
                      </div>
                      
                      <div class="boxes-container">
                        <div class="box1-left-content">Всего аккаунтов: <?=R::count( 'users' );?></div>
                  
                        <div class="box1-left-content">Количество устройств: <?=R::count( 'devices' );?></div>
                      </div>
                    </div>
                </div>
            </div>
            
            <div id="change_panel" class="tabcontent">
              <div class="row first-row">
                <div class="menu">
                  <i class="fa fa-bars" aria-hidden="true"></i>
                </div>
                <div class="page-header">Настройки</div>
              </div>
            </div>
            
          </section>
        </container>
      	
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

  function del(login,sid)
  {
    

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