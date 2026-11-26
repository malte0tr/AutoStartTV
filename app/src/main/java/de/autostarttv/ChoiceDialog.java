package de.autostarttv;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/** A plain dialog in the app's style: title, optional message, list of choices. */
final class ChoiceDialog {

    interface Listener {
        void onChosen(int index);
    }

    private ChoiceDialog() {
    }

    /**
     * @param icons    optional icons (same size as labels), or null
     * @param selected index of the current choice, or -1
     */
    static Dialog show(Context context, CharSequence title, CharSequence message,
                       List<? extends CharSequence> labels, List<Drawable> icons,
                       int selected, Listener listener) {
        Dialog dialog = new Dialog(context, R.style.SwissDialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_choice);

        TextView titleView = dialog.findViewById(R.id.dialog_title);
        titleView.setText(title);
        TextView messageView = dialog.findViewById(R.id.dialog_message);
        if (message != null) {
            messageView.setText(message);
            messageView.setVisibility(View.VISIBLE);
        }

        ListView list = dialog.findViewById(R.id.dialog_list);
        list.setAdapter(new Adapter(context, labels, icons, selected));
        list.setOnItemClickListener((parent, view, position, id) -> {
            dialog.dismiss();
            if (listener != null) {
                listener.onChosen(position);
            }
        });

        Window window = dialog.getWindow();
        if (window != null) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            int maxWidth = context.getResources().getDimensionPixelSize(R.dimen.dialog_max_width);
            int width = Math.min((int) (metrics.widthPixels * 0.9f), maxWidth);
            window.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.show();
        list.requestFocus();
        if (selected >= 0) {
            list.setSelection(selected);
        }
        return dialog;
    }

    /** Caches the item views, so scrolling long lists stays smooth. */
    private static final class Holder {
        final TextView label;
        final ImageView icon;
        final View marker;

        Holder(View view) {
            label = view.findViewById(R.id.item_label);
            icon = view.findViewById(R.id.item_icon);
            marker = view.findViewById(R.id.item_marker);
        }
    }

    private static final class Adapter extends BaseAdapter {
        private final LayoutInflater inflater;
        private final List<? extends CharSequence> labels;
        private final List<Drawable> icons;
        private final int selected;

        Adapter(Context context, List<? extends CharSequence> labels, List<Drawable> icons,
                int selected) {
            this.inflater = LayoutInflater.from(context);
            this.labels = labels;
            this.icons = icons;
            this.selected = selected;
        }

        @Override
        public int getCount() {
            return labels.size();
        }

        @Override
        public Object getItem(int position) {
            return labels.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            Holder holder;
            if (view == null) {
                view = inflater.inflate(R.layout.choice_item, parent, false);
                holder = new Holder(view);
                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }
            holder.label.setText(labels.get(position));
            if (icons != null) {
                holder.icon.setImageDrawable(icons.get(position));
                holder.icon.setVisibility(View.VISIBLE);
            } else {
                holder.icon.setVisibility(View.GONE);
            }
            holder.marker.setVisibility(position == selected ? View.VISIBLE : View.INVISIBLE);
            return view;
        }
    }
}
