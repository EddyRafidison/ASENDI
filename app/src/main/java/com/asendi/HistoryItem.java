package com.asendi;

import android.content.Context;
import androidx.core.text.HtmlCompat;

public class HistoryItem {
    String time,
    amount,
    receiver,
    reference;
    String type;
    Context ctx;
    String account;
    String fees;
    public HistoryItem(Context ctx, String time, String amount, String type, String receiver, String reference, String fees) {
        // type is either 0 (sent) or 1 (received)
        this.time = time;
        this.ctx = ctx;
        this.amount = amount;
        this.type = type;
        this.receiver = receiver;
        this.reference = reference;
        this.fees = fees;
        account = Utils.getAccount(ctx);
    }

    public int getDrawable() {
        if (type.equals("1") || type.equals("2")) {
            return R.drawable.arrow_top_left;
        } else {
            return R.drawable.arrow_bottom_right;
        }
    }

    public String getDMYTime() {
        // get DMY from YMD mode
        String d = time.substring(6, 8);
        String m = time.substring(4, 6);
        String y = time.substring(0, 4);
        return d+"."+m+"."+y;
    }
    public String getHMTime() {
        String h = time.substring(8, 10);
        String min = time.substring(10, 12);
        String ss = time.substring(12, 14);
        return h+":"+ min +"."+ss;
    }
    public CharSequence getDetail() {
        //Note : SU = Scalable Unit
        if (type.equals("1")) {
            return HtmlCompat.fromHtml("<b>" + amount + "</b>" + "&nbsp;" + ctx.getString(R.string.to) + "&nbsp;"
                + receiver + (".&nbsp;").replace(".", ctx.getString(R.string.punct)) + ctx.getString(R.string.fees).replace("100", fees) + (".<br>").replace(".", ctx.getString(R.string.punct)) + "Id:&nbsp;" + reference, HtmlCompat.FROM_HTML_MODE_LEGACY);
        } else if (type.equals("2")) {
            return HtmlCompat.fromHtml("<b>" + amount + "</b>" + "&nbsp;" + ctx.getString(R.string.to) + "&nbsp;"
                + receiver + (".&nbsp;").replace(".", ctx.getString(R.string.punct)) + ctx.getString(R.string.fees).replace("100", fees)+(".&nbsp;").replace(".", ctx.getString(R.string.punct)) + ctx.getString(R.string.burn_type) + (".<br>").replace(".", ctx.getString(R.string.punct)) + "Id:&nbsp;" + reference, HtmlCompat.FROM_HTML_MODE_LEGACY);
        } else {
            return HtmlCompat.fromHtml("<b>" + amount + "</b>" + "&nbsp;" + ctx.getString(R.string.from) + "&nbsp;"
                + receiver + ctx.getString(R.string.punct) + "<br>" + "Id:&nbsp;" + reference, HtmlCompat.FROM_HTML_MODE_LEGACY);
        }
    }
}