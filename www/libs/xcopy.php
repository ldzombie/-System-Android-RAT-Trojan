<?php 


setlocale(LC_ALL, 'ru_RU.cp1251');
date_default_timezone_set('Asia/Irkutsk');

function copy_folder($d1, $d2,$dlog, $upd = true, $force = true) {
    if ( is_dir( $d1 ) ) {
        $d2 = mkdir_safe( $d2, $force );
        if (!$d2) {fs_log($dlog,"!!fail $d2"); return;}
        $d = dir( $d1 );
        while ( false !== ( $entry = $d->read() ) ) {
            if ( $entry != '.' && $entry != '..' ) 
                copy_folder( "$d1/$entry", "$d2/$entry",$dlog, $upd, $force );
        }
        $d->close();
    }
    else {
        $ok = copy_safe( $d1, $d2, $upd );
        $ok = ($ok) ? "ok-- " : " -- ";
        fs_log($dlog,"{$ok}$d1"); 
    }
} //function copy_folder

function mkdir_safe( $dir, $force ) {
    if (file_exists($dir)) {
        if (is_dir($dir)) return $dir;
        else if (!$force) return false;
        unlink($dir);
    }
    return (mkdir($dir, 0777, true)) ? $dir : false;
} //function mkdir_safe

function copy_safe ($f1, $f2, $upd) {
    $time1 = filemtime($f1);
    if (file_exists($f2)) {
        $time2 = filemtime($f2);
        if ($time2 >= $time1 && $upd) return false;
    }
    $ok = copy($f1, $f2);
    if ($ok) touch($f2, $time1);
    return $ok;
} //function copy_safe 

function fs_log($d , $str) {
    $log = fopen($d, "a");
    $time = date("Y-m-d H:i:s");
    fwrite($log, "$str ($time)"."\r\n");
    fclose($log);
}

?>