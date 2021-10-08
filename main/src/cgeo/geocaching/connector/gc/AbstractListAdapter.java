package cgeo.geocaching.connector.gc;

import cgeo.geocaching.CacheListActivity;
import cgeo.geocaching.R;
import cgeo.geocaching.databinding.GclistItemBinding;
import cgeo.geocaching.models.GCList;
import cgeo.geocaching.storage.extension.PocketQueryHistory;
import cgeo.geocaching.ui.recyclerview.AbstractRecyclerViewHolder;
import cgeo.geocaching.utils.Formatter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.apache.commons.lang3.StringUtils;
import org.mapsforge.map.rendertheme.renderinstruction.Line;

import java.util.ArrayList;

class AbstractListAdapter extends RecyclerView.Adapter<AbstractListAdapter.ViewHolder> {

    @NonNull private final AbstractListActivity activity;
    final ArrayList<GCList> pocketQueryArrayList = new ArrayList<GCList>();

    protected static final class ViewHolder extends AbstractRecyclerViewHolder {
        private final GclistItemBinding binding;

        ViewHolder(final View view) {
            super(view);
            binding = GclistItemBinding.bind(view);
        }
    }

    AbstractListAdapter(@NonNull final AbstractListActivity abstractListActivity) {
        this.activity = abstractListActivity;
    }

    @Override
    public int getItemCount() {
        return activity.getQueries().size();
    }

    private int countCaches() {
        int countCaches = 0;
        for (int i = 0; i < pocketQueryArrayList.size(); i++) {
            GCList gcListTmp = pocketQueryArrayList.get(i);
            countCaches += gcListTmp.getCaches();
        }
        return countCaches;
    }

    private void updateGcListHeaderButton() {
        final LinearLayout ll2 = (LinearLayout)activity.findViewById(R.id.gclist_header);
        if ( pocketQueryArrayList.size() == 0 ) {
            ll2.setVisibility(View.GONE);
        } else {
            ll2.setVisibility(View.VISIBLE);
        }
        final TextView t2 = (TextView)activity.findViewById(R.id.gclist_header_summary);
        t2.setText(pocketQueryArrayList.size() + " Pocket queries\n" + countCaches() + " Caches");
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gclist_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);

        viewHolder.binding.cachelist.setOnClickListener(view1 -> CacheListActivity.startActivityPocket(view1.getContext(), activity.getQueries().get(viewHolder.getAdapterPosition())));

        //final CachedetailWaypointsHeaderBinding headerBinding = CachedetailWaypointsHeaderBinding.inflate(getLayoutInflater(), v, false);
        //final GclistHeaderBinding headerBinding = GclistHeaderBinding.inflate(LayoutInflater.from(view.getContext()), parent, false);
        //final ListView l = (ListView) parent.getParent();
        //l.addHeaderView(headerBinding.getRoot());

        viewHolder.binding.download.setOnClickListener(new View.OnClickListener() {
            public void onClick(final View v) {
                final Button b = v.findViewById(R.id.download);
                final GCList pocketQuery = activity.getQueries().get(viewHolder.getAdapterPosition());
                if (pocketQueryArrayList.contains(pocketQuery)) {
                    pocketQueryArrayList.remove(pocketQuery);
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save, 0, 0, 0);
                } else {
                    if (pocketQueryArrayList.size() <= 0) {
                        PocketQueryHistory.updateLastDownload(pocketQuery);
                        notifyDataSetChanged();

                        if (activity.getStartDownload()) {
                            CacheListActivity.startActivityPocketDownload(v.getContext(), pocketQuery);
                        } else {
                            activity.returnResult(pocketQuery);
                        }
                    } else {
                        pocketQueryArrayList.add(pocketQuery);
                        b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save_selected, 0, 0, 0);
                        updateGcListHeaderButton();
                    }
                }
            }
        });

        viewHolder.binding.download.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final Button b = view.findViewById(R.id.download);
                final GCList pocketQuery = activity.getQueries().get(viewHolder.getAdapterPosition());
                if (pocketQueryArrayList.contains(pocketQuery)) {
                    pocketQueryArrayList.remove(pocketQuery);
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save, 0, 0, 0);
                    updateGcListHeaderButton();
                } else {
                    pocketQueryArrayList.add(pocketQuery);
                    b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save_selected, 0, 0, 0);
                    updateGcListHeaderButton();
                }
                return true;
            }
            /*
            final GCList pocketQuery = activity.getQueries().get(viewHolder.getAdapterPosition());
            if (pocketQueryArrayList.contains(pocketQuery)) {
                pocketQueryArrayList.remove(pocketQuery);
                v.setBackgroundResource(R.drawable.ic_menu_save);
            } else {
                pocketQueryArrayList.add(pocketQuery);
                v.setBackgroundResource(R.drawable.ic_menu_save_selected);
            }*/
            //activity.findViewById(R.id.download).setBackgroundResource(R.drawable.ic_menu_save_selected);
            //v.setBackgroundResource(R.drawable.ic_menu_save_selected);
            //return true;
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final GCList pocketQuery = activity.getQueries().get(position);
        final Button b = (Button) holder.binding.download;
        b.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_menu_save, 0, 0, 0);
        holder.binding.download.setVisibility(pocketQuery.isDownloadable() ? View.VISIBLE : View.GONE);
        holder.binding.cachelist.setVisibility(activity.onlyDownloadable() || pocketQuery.isBookmarkList() ? View.GONE : View.VISIBLE); // Currently, we aren't able to parse bookmark lists without download
        holder.binding.label.setText(pocketQuery.getName());
        final String info = Formatter.formatPocketQueryInfo(pocketQuery);
        holder.binding.info.setVisibility(StringUtils.isNotBlank(info) ? View.VISIBLE : View.GONE);
        holder.binding.info.setText(info);
    }

}
