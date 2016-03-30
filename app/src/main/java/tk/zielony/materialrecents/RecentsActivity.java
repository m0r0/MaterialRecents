package tk.zielony.materialrecents;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.moro.materialrecents.RecentsAdapter;
import com.moro.materialrecents.RecentsList;
import java.util.Random;

/**
 * Created by Marcin on 2015-04-14.
 */
public class RecentsActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.materialrecents_activity_recents);

    final int[] colors = new int[] { 0xff7fffff, 0xffff7fff, 0xffffff7f, 0xff7f7fff, 0xffff7f7f, 0xff7fff7f };
    final Random random = new Random();

    RecentsList recents = (RecentsList) findViewById(R.id.recents);
    RecentsAdapter adapter = new RecentsAdapter() {

      @Override public View getView(ViewGroup parent, int position) {
        final View card = getLayoutInflater().inflate(R.layout.materialrecents_recent_card, parent, false);
        TextView title = (TextView) card.findViewById(R.id.card_title);
        title.setText("Item " + position);
        ImageView icon = (ImageView) card.findViewById(R.id.card_icon);
        Drawable drawable = getResources().getDrawable(R.mipmap.ic_launcher);
        if (drawable == null) {
          icon.setVisibility(View.GONE);
        } else {
          icon.setImageDrawable(drawable);
        }
        View header = card.findViewById(R.id.card_header);
        header.setBackgroundColor(colors[random.nextInt(colors.length)]);
        ImageView iv = (ImageView) card.findViewById(R.id.image);
        iv.setImageResource(R.drawable.mazda);
        return card;
      }

      @Override public int getCount() {
        return 5;
      }
    };
    recents.setAdapter(adapter);

    recents.setOnItemClickListener(new RecentsList.OnItemClickListener() {
      @Override public void onItemClick(View view, int i) {
        Toast.makeText(view.getContext(), "Card " + i + " clicked", Toast.LENGTH_SHORT).show();
      }
    });
  }
}
