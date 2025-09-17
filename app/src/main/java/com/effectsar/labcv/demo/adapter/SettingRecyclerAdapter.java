package com.effectsar.labcv.demo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.effectsar.labcv.demo.R;
import com.effectsar.labcv.demo.model.SettingModel;

import java.lang.ref.WeakReference;
import java.util.List;

public class SettingRecyclerAdapter extends RecyclerView.Adapter<SettingRecyclerAdapter.ViewHolder> {

    private List<SettingModel> settingModels;

    private WeakReference<ItemConfirmListener> mLisenter = new WeakReference<>(null);

    public SettingRecyclerAdapter(List<SettingModel> settingModels) {
        this.settingModels = settingModels;
    }

    public void setOnItemConfirmListener(ItemConfirmListener li) {
        mLisenter = new WeakReference<>(li);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int rid = R.layout.item_setting_show;
        switch (viewType) {
            case 0: // show type
                rid = R.layout.item_setting_show;
                break;
            case 1: // input type
                rid = R.layout.item_setting_input;
                break;
            case 2: // switch type
                rid = R.layout.item_setting_switch;
                break;
            case 3: // triiger type
                rid = R.layout.item_setting_trigger;
                break;
            default:
                break;
        }
        View v = LayoutInflater.from(parent.getContext()).inflate(rid, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvTitle.setText(settingModels.get(position).getEnumType().value());
        if (getItemViewType(position) == SettingModel.SettingItemShowType.ST_SHOW.value()) { // show
            holder.tvContent.setText(settingModels.get(position).getContent());
        } else if (getItemViewType(position) == SettingModel.SettingItemShowType.ST_INPUT.value()) { // input
            holder.etContent.setHint(settingModels.get(position).getHintText());
            holder.etContent.setText(settingModels.get(position).getContent());
            holder.btnConfirm.setOnClickListener(v -> {
                if (mLisenter.get() != null) {
                    mLisenter.get().onInputConfirm(settingModels.get(position), holder.etContent.getText().toString());
                }
            });
        } else if (getItemViewType(position) == SettingModel.SettingItemShowType.ST_SWITCH.value()) { // switch
            holder.swToggle.setChecked(!"0".equals(settingModels.get(position).getContent()));
            holder.tvHint.setText(settingModels.get(position).getHintText());
            holder.swToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (mLisenter.get() != null) {
                    mLisenter.get().onSwtichToggle(settingModels.get(position), holder.swToggle.isChecked());
                }
            });
        } else if (getItemViewType(position) == SettingModel.SettingItemShowType.ST_TRIGGER.value()) {
            holder.tvHint.setText(settingModels.get(position).getHintText());
            holder.btnTrigger.setOnClickListener(v -> {
                if (mLisenter.get() != null) {
                    mLisenter.get().onInputConfirm(settingModels.get(position), "");
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return settingModels.get(position).getType().value();
    }

    @Override
    public int getItemCount() {
        return settingModels == null ? 0 : settingModels.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvContent;
        private EditText etContent;
        private Button btnConfirm;
        private Switch swToggle;

        private TextView tvHint;

        private Button btnTrigger;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            etContent = itemView.findViewById(R.id.etContent);
            btnConfirm = itemView.findViewById(R.id.btnConfirm);
            swToggle = itemView.findViewById(R.id.swToggle);
            btnTrigger = itemView.findViewById(R.id.btnTrigger);
            tvHint = itemView.findViewById(R.id.tvHint);
        }
    }

    public interface ItemConfirmListener {
        void onInputConfirm(SettingModel model, String content);
        void onSwtichToggle(SettingModel model, boolean on);
    }
}
