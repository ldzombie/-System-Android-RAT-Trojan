package oom.android.system.Managers.Listeners;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.widget.Toast;

import oom.android.system.R;

public class DeviceAdminSample extends DeviceAdminReceiver {

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        // Вызывается перед тем, как данное приложение перестанет
        // быть администратором устройства (будет отключено
        // пользователем).
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        // Вызывается, когда пользователь разрешил использовать
        // этот приложение как администратор устройства.
        // Здесь можно использовать DevicePolicyManager
        // для установки политик администрирования.
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        // Вызывается после смены пароля пользователем.
        // Соответствует ли новый пароль политикам,
        // можно узнать с помощью метода
        // DevicePolicyManager.isActivePasswordSufficient()
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        super.onPasswordExpiring(context, intent);
        // Вызывается несколько раз при приближении времени
        // устаревания пароля: при включении устройства, раз в день
        // перед устареванием пароля и в момент устаревания пароля.
        // Если пароль не был изменен после устаревания, метод
        // вызывается раз в день
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        // Вызывается в случае ввода неправильного пароля.
        // Количество неудачных попыток ввода пароля можно узнать
        // с помощью метода getCurrentFailedPasswordAttempts()
        // класса DevicePolicyManager.
    }

}
