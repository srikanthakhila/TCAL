package com.orawaves.android.adapters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.orawaves.tcal.R;
import com.orawaves.tcal.andorid.dto.DTO;
import com.orawaves.tcal.andorid.dto.TimelineDTO;
import com.orawaves.tcal.andorid.socialnetwork.FacebookPost;
import com.orawaves.tcal.andorid.socialnetwork.TwitterPost;
import com.orawaves.tcal.andorid.socialnetwork.FacebookPost.FbCallBack;
import com.orawaves.tcal.android.dao.TimelineDAO;
import com.orawaves.tcal.android.database.DBHandler;
import com.twitter.android.Twitt_Sharing.TwitterCallback;


public class PendingTimelineListAdapter extends BaseAdapter 
{
	private List<DTO> dataList;
	private LayoutInflater inflater;
	Activity context;
	private String shareContent = "";

	public PendingTimelineListAdapter(Activity context, List<DTO> dataList)
	{
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		this.dataList	= dataList;
		this.context	= context;
	}

	@Override
	public int getCount()
	{
		return dataList.size();

	}

	@Override
	public Object getItem(int position) 
	{
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		final TimelineDTO	dto	= (TimelineDTO) dataList.get(position); 

		if(convertView == null)
			convertView	 = inflater.inflate(R.layout.posttimeline_list_adapter, null);


		TextView txtHeader	= (TextView) convertView.findViewById(R.id.txtToShare);
		final ImageView del = (ImageView) convertView.findViewById(R.id.imgDeleteIcon);
		final ImageView fb = (ImageView) convertView.findViewById(R.id.fb_s);
		final ImageView tw = (ImageView) convertView.findViewById(R.id.tw_s); 
		final ImageView em = (ImageView) convertView.findViewById(R.id.em_s);

		String shareOptions = dto.getmShare();

		if (shareOptions.contains("f")) {
			fb.setVisibility(View.VISIBLE);
		}
		else
		{
			fb.setVisibility(View.GONE);
		}

		if (shareOptions.contains("t"))
		{
			tw.setVisibility(View.VISIBLE);
		}
		else
		{
			tw.setVisibility(View.GONE);
		}
		if (shareOptions.contains("e")) 
		{
			em.setVisibility(View.VISIBLE);
		}
		else
		{
			em.setVisibility(View.GONE);
		}

		txtHeader.setText(dto.getContent());



		//Click function when we click on delete icon
		del.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) { 

				Toast.makeText(context, "Delete", 10).show();

				updateGUI(position);
			}
		});

		//When you click on FB implementation 
		fb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (dto.getCtype().equalsIgnoreCase("text") || dto.getCtype().equalsIgnoreCase("location")) {
					if (dto.getmShare().contains("f")) {
						shareContent = dto.getContent();
					}
					Bundle bundle = new Bundle();				
					bundle.putString("message", shareContent.replaceAll("\\&", "%26") );

					new FacebookPost(context, new FbCallBack() {

						@Override
						public void callback() {

							Toast toast=Toast.makeText(context.getApplicationContext(), "posted successfully on your facebook wall.", Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();

							updateGUI(position); 
							fb.setVisibility(View.INVISIBLE);
						}
					}).post(shareContent.replaceAll("\\&", "%26"));
				}
				if (dto.getCtype().equalsIgnoreCase("image")) {
					try {
						if (dto.getmShare().contains("f")) {
							shareContent = dto.getContent();
						}
						Bundle bundle = new Bundle();				
						//bundle.putString("message", shareContent.replaceAll("\\&", "%26") );
						File imageFile = new File(shareContent);

						//	byte[] data = null;
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						Bitmap bi = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
						new FacebookPost(context, new FbCallBack() {

							@Override
							public void callback() {

								Toast toast=Toast.makeText(context.getApplicationContext(), "posted successfully on your facebook wall.", Toast.LENGTH_LONG);
								toast.setGravity(Gravity.CENTER, 0, 0);
								toast.show();

								updateGUI(position); 
								fb.setVisibility(View.INVISIBLE);
							}
						}).post(bi);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}
		});


		//Twitter implementation 
		tw.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (dto.getCtype().equalsIgnoreCase("text") || dto.getCtype().equalsIgnoreCase("location")) {
					if (dto.getmShare().contains("t")) {
						shareContent = dto.getContent();
					}
					new TwitterPost().Post(context, shareContent, new TwitterCallback() {

						@Override
						public void twitterCall() {
							Toast.makeText(context, "Twitted post sucessfully", Toast.LENGTH_LONG).show();
							tw.setVisibility(View.INVISIBLE);
						}
					});

				}

				if (dto.getCtype().equalsIgnoreCase("image") || dto.getCtype().equalsIgnoreCase("location")) {

					if (dto.getmShare().contains("t")) {
						shareContent = dto.getContent();
					}
					new TwitterPost().Post(context,"" ,new File(shareContent),null, new TwitterCallback() {

						@Override
						public void twitterCall() {
							Toast.makeText(context, "Twitted post sucessfully", Toast.LENGTH_LONG).show();
							tw.setVisibility(View.INVISIBLE);
						}
					});
				}


			}
		});

		//Email implementation 
		em.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (dto.getmShare().contains("e")) {
					shareContent = dto.getContent();

					Intent send = new Intent(Intent.ACTION_SENDTO);
					String uriText = "mailto:" + Uri.encode(dto.getmShareEmail()) + 
							"?subject=" + Uri.encode("Timeline message") + 
							"&body=" + Uri.encode(dto.getContent());
					Uri uri = Uri.parse(uriText);

					send.setData(uri);
					context.startActivity(Intent.createChooser(send, "Send mail..."));
					em.setVisibility(View.INVISIBLE);
				}

			}
		});

		return convertView;
	}

	private void updateGUI(final int position)
	{
		final TimelineDTO tdto = (TimelineDTO)dataList.get(position); 
		new AlertDialog.Builder(context)
		.setTitle("Delete Timeline")
		.setMessage(tdto.getContent())
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				boolean isDele = TimelineDAO.getInstance().delete(tdto, DBHandler.getInstance(context).getWritableDatabase());
				if (isDele) {

					Toast.makeText(context, "Timeline deleted sucessfully", Toast.LENGTH_LONG).show();
					dataList.remove(position);
					notifyDataSetChanged();
				}
				dialog.dismiss();
			}
		})
		.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) { 
				dialog.dismiss();
			}
		})
		.setIcon(android.R.drawable.ic_dialog_alert)
		.show(); 
	}
}
