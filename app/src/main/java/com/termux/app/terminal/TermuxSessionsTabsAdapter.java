package com.termux.app.terminal;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.termux.R;
import com.termux.app.TermuxActivity;
import com.termux.shared.termux.shell.command.runner.terminal.TermuxSession;
import com.termux.shared.theme.NightMode;
import com.termux.shared.theme.ThemeUtils;
import com.termux.terminal.TerminalSession;

import java.util.List;

public class TermuxSessionsTabsAdapter extends RecyclerView.Adapter<TermuxSessionsTabsAdapter.TabViewHolder> {

    final TermuxActivity mActivity;
    final List<TermuxSession> mSessionList;

    final StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
    final StyleSpan italicSpan = new StyleSpan(Typeface.ITALIC);

    public TermuxSessionsTabsAdapter(TermuxActivity activity, List<TermuxSession> sessionList) {
        this.mActivity = activity;
        this.mSessionList = sessionList;
    }

    @NonNull
    @Override
    public TabViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tab_session, parent, false);
        return new TabViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TabViewHolder holder, int position) {
        TermuxSession termuxSession = mSessionList.get(position);
        TerminalSession session = termuxSession.getTerminalSession();

        if (session == null) {
            holder.titleView.setText("null session");
            return;
        }

        boolean shouldEnableDarkTheme = ThemeUtils.shouldEnableDarkTheme(mActivity, NightMode.getAppNightMode().getName());

        boolean isCurrentSession = mActivity.getCurrentSession() == session;

        if (isCurrentSession) {
            holder.itemView.setBackgroundColor(shouldEnableDarkTheme ? 0xFF444444 : 0xFFDDDDDD);
        } else {
            android.util.TypedValue outValue = new android.util.TypedValue();
            mActivity.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            holder.itemView.setBackgroundResource(outValue.resourceId);
        }

        String name = session.mSessionName;
        String sessionTitle = session.getTitle();

        String numberPart = "[" + (position + 1) + "] ";
        String sessionNamePart = (TextUtils.isEmpty(name) ? "" : name);
        String sessionTitlePart = (TextUtils.isEmpty(sessionTitle) ? "" : ((sessionNamePart.isEmpty() ? "" : " ") + sessionTitle));

        String fullSessionTitle = numberPart + sessionNamePart + sessionTitlePart;
        SpannableString fullSessionTitleStyled = new SpannableString(fullSessionTitle);
        fullSessionTitleStyled.setSpan(boldSpan, 0, numberPart.length() + sessionNamePart.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fullSessionTitleStyled.setSpan(italicSpan, numberPart.length() + sessionNamePart.length(), fullSessionTitle.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        holder.titleView.setText(fullSessionTitleStyled);

        boolean sessionRunning = session.isRunning();
        if (sessionRunning) {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.titleView.setPaintFlags(holder.titleView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        
        int defaultColor = shouldEnableDarkTheme ? Color.WHITE : Color.BLACK;
        int color = sessionRunning || session.getExitStatus() == 0 ? defaultColor : Color.RED;
        holder.titleView.setTextColor(color);

        holder.itemView.setOnClickListener(v -> {
            mActivity.getTermuxTerminalSessionClient().setCurrentSession(session);
            notifyDataSetChanged();
        });

        holder.menuButton.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(mActivity, v);
            popup.getMenu().add(0, 1, 0, "Rename");
            popup.getMenu().add(0, 2, 0, "Close");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1) {
                    mActivity.getTermuxTerminalSessionClient().renameSession(session);
                    return true;
                } else if (item.getItemId() == 2) {
                    session.finishIfRunning();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return mSessionList.size();
    }

    static class TabViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        ImageButton menuButton;

        public TabViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.tab_title);
            menuButton = itemView.findViewById(R.id.tab_menu_button);
        }
    }
}
