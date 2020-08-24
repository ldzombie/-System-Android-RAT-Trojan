<?php
define('UPLOAD_DIR', 'img/');
$img = $_POST['savephotob64'];
$img = str_replace('data:image/jpeg;base64,', '', $img);
$data = base64_decode($img);
$file = UPLOAD_DIR.uniqid().'.png';
$success = file_put_contents($file, $data);

?>