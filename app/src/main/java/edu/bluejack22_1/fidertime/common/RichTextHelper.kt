package edu.bluejack22_1.fidertime.common

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import android.widget.TextView
import edu.bluejack22_1.fidertime.activities.MessagePersonalDetailActivity
import java.util.regex.Pattern

class RichTextHelper {
    companion object {
        fun linkAndMentionRecognizer(context: Context, textView: TextView, text: String) {
            val spanned = SpannableString(text)
            val linkMatcher = Patterns.WEB_URL.matcher(text)
            val mentionMatcher = Pattern.compile("(@[A-Za-z0-9_-]+)").matcher(text)
            var matchStart: Int
            var matchEnd: Int

            while (linkMatcher.find()) {

                matchStart = linkMatcher.start(1)
                matchEnd = linkMatcher.end()

                var url = text.substring(matchStart, matchEnd)
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://$url"
                }

                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.BLUE
                        ds.isUnderlineText = true
                    }
                }

                spanned.setSpan(
                    clickableSpan,
                    matchStart,
                    matchEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

            }

            while (mentionMatcher.find()) {
                matchStart = mentionMatcher.start(1)
                matchEnd = mentionMatcher.end()

                val username = text.substring(matchStart + 1, matchEnd)

                val clickableSpan: ClickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        val intent = Intent(context, MessagePersonalDetailActivity::class.java)
                        intent.putExtra("username", username)
                        context.startActivity(intent)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.color = Color.RED
                        ds.isUnderlineText = false
                    }
                }

                spanned.setSpan(
                    clickableSpan,
                    matchStart,
                    matchEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            textView.text = spanned
            textView.movementMethod = LinkMovementMethod.getInstance()
        }
    }
}