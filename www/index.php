
<?php
	require "db.php";
	if(isset($_SESSION['logged_used']))
		header('Location: /home');

	$data = $_POST;
	$errors = array();
	if(isset($data['do_login'])){
		$user = R::findOne('users','login=?',array($data['login']));
		if($user){
			if(password_verify($data['password'], $user->password)){
				//logining
				$_SESSION['logged_used']=$user;
				$_SESSION['sid'] = $user->sid;
				if($user->admin==1)
					$_SESSION['admin']=true;
				header('Location: /home');
				
			}else{
				$errors[] = "Неверный пароль";
			}

		}else{
			$errors[] = "Пользователь не найден";
		}
		if(!empty($errors)){
			print '<div class="msg-error" style="color:red;">'.array_shift($errors).'</div><br>';
		}
	} ?>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">



<head>
	<meta charset="UTF-8">
  <title>Login</title>
  <link rel="stylesheet" href="./style/login.css">
</head>
<body>
	
	<div class="login">
		<div class="login-triangle"></div>
		<h2 class="login-header">Log in</h2>
		<form action="/index.php" method="POST" class="login-container" autocomplete="off">
			<p><input type="login" name="login" placeholder="Login" value="<?php if(isset($data['login'])){ echo $data['login']; } ?>"></p>
			<p><input type="password" name="password" placeholder="Password" value="<?php if(isset($data['password'])){ echo $data['password']; } ?>"></p>
			<p><button type="submit" name="do_login">Login</button></p>
		</form>
	</div>
	
</body>

</html>
