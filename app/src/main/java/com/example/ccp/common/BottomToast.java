package com.example.ccp.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import com.example.ccp.databinding.ToastBottomBinding;

public class BottomToast {
    public static Toast createToast(Context ctx, String msg) {
        LayoutInflater lif = LayoutInflater.from(ctx);
        ToastBottomBinding binding = ToastBottomBinding.inflate(lif);
        binding.tvMsg.setText(msg);
        Toast toast = new Toast(ctx);
        toast.setGravity(
            Gravity.BOTTOM | Gravity.CENTER | Gravity.FILL_HORIZONTAL, 0, Convert.dpToPx(ctx, 100));
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(binding.getRoot());
        return toast;
    }

    public static Toast createToast(Context ctx, String msg, int length) {
        LayoutInflater lif = LayoutInflater.from(ctx);
        ToastBottomBinding binding = ToastBottomBinding.inflate(lif);
        binding.tvMsg.setText(msg);
        Toast toast = new Toast(ctx);
        toast.setGravity(
                Gravity.BOTTOM | Gravity.CENTER | Gravity.FILL_HORIZONTAL, 0, Convert.dpToPx(ctx, 100));
        toast.setDuration(length);
        toast.setView(binding.getRoot());
        return toast;
    }
}
