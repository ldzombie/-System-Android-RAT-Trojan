<?php
	require "db.php";

	$data = $_POST;
	$sid=1;
	$scount=0;
	if(isset($data['do_signup'])){
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

		if(R::count('users',"login=? AND sid=?",array($data['login'],$sid)) > 0){

			$errors[] = "Пользователь с таким логином и идентификатором уже существует";
		}
		$scount=R::count('users',"sid=?",array($sid));
		while($scount>0){
			$sid++;
			$scount=R::count('users',"sid=?",array($sid));
		}
		if(empty($errors)){
			$user = R::dispense('users');
			$user->admin=false;
			$user->login = $data['login'];
			$user->password = password_hash($data['password'], PASSWORD_DEFAULT);
			$user->sid = $sid;
			$user->devices=0;
			R::store($user);

			echo '<div style="color:green;">Вы успешно зарегистрированны</div><br>';

		}else{
			echo '<div style="color:red;">'.array_shift($errors).'</div><br>';
		}
	}
?>

<form action="/signup.php" method="POST">
	<p>
		<p><strong>Login</strong>:</p>
		<input type="text" name="login" value="<?php echo $data['login']; ?>">
	</p>
	<p>
		<p><strong>Password</strong>:</p>
		<input type="password" name="password" value="<?php echo $data['password']; ?>">
	</p>
	<p>
		<p><strong>Password</strong>:</p>
		<input type="password" name="password_2" >
	</p>
	<p>
		<button type="submit" name="do_signup">Sign Up</button>
	</p>
</form>