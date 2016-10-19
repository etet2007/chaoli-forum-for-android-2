package com.geno.chaoli.forum.viewmodel;

import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.geno.chaoli.forum.view.HomepageActivity;
import com.geno.chaoli.forum.view.IView;

/**
 * Created by jianhao on 16-9-21.
 */

public class HomepageViewModel extends BaseViewModel {
    HomepageActivity view;

    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> signature = new ObservableField<>();
    public ObservableField<String> avatarSuffix = new ObservableField<>();
    public ObservableInt userId = new ObservableInt();
    public ObservableBoolean isSelf = new ObservableBoolean();

    public HomepageViewModel(String username, String signature, String avatarSuffix, int userId, Boolean isSelf) {
        this.username.set(username);
        this.userId.set(userId);
        this.signature.set(signature);
        this.avatarSuffix.set(avatarSuffix);
        this.isSelf.set(isSelf);
    }
}
