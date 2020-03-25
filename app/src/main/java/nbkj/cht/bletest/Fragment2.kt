package nbkj.cht.bletest

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager

import android.widget.TextView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.widget.Button
import java.util.*


class Fragment2 : Fragment() {
    val adapter = mAdapter()
    var mDatas_name = ArrayList<String>(Arrays.asList(
        "倾斜角：","方位角：","自转角：","重力模量：","地磁模量：","磁倾角：","Ax：","Ay：","Az：","Mx：","My：","Mz：","电池电压："))
    var mDatas = ArrayList<String>()
    var Sample_EN = false
    var timer_sample: Timer? = null;
    var timer_task: TimerTask? = null;


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_fragment2, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        mRecyclerView.setLayoutManager(LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false))


        var i: Int = 'A'.toInt()
        while (i <= 'M'.toInt())
        {
            //mDatas_name.add("" + i.toChar())
            mDatas.add("" + i.toChar())
            i++
        }


        mRecyclerView.setAdapter(adapter)

        var Sample_Btn = view.findViewById<Button>(R.id.btn_ADTest);
        Sample_Btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if(Sample_EN) {
                    timer_sample_dis();
                    Sample_Btn.setText(R.string.btn_ADTest_on)
                    Sample_Btn.setTextColor(Color.BLACK)
                }
                else {
                    timer_sample_en();
                    Sample_Btn.setText(R.string.btn_ADTest_off)
                    Sample_Btn.setTextColor(Color.RED)
                }
            }
        })

        return view;
    }

    fun timer_sample_dis()
    {
        Sample_EN = false;

        timer_sample?.cancel()
        timer_task?.cancel()
        timer_sample = null;
        timer_task = null;
    }
    fun timer_sample_en()
    {
        Sample_EN = true

        if(timer_sample == null)
            timer_sample = Timer();
        if(timer_task == null)
        {
            timer_task = object : TimerTask() {
                override fun run() {
                    MainActivity.cmd_send();
                }
            }
        }
        timer_sample?.schedule(timer_task, 100, 1000);
    }

    fun get_mAdapter():mAdapter{
        return adapter;
    }
    fun get_mDatas():ArrayList<String>
    {
        return mDatas;
    }

    fun update_mDatas(byteArray: ByteArray)
    {
        var i16_val: Short;

        i16_val = ( (byteArray[9].toInt() and 0xff shl 8) + (byteArray[8].toInt() and 0xff)).toShort()
        mDatas.set(6, String.format("%.3fg", i16_val/1000.0))
        i16_val = ((byteArray[11].toInt() and 0xff shl 8) + (byteArray[10].toInt() and 0xff)).toShort()
        mDatas.set(7, String.format("%.3fg", i16_val/1000.0))
        i16_val = ((byteArray[13].toInt() and 0xff shl 8) + (byteArray[12].toInt() and 0xff)).toShort()
        mDatas.set(8, String.format("%.3fg", i16_val/1000.0))

        i16_val = ( (byteArray[15].toInt() and 0xff shl 8) + (byteArray[14].toInt() and 0xff)).toShort()
        mDatas.set(9, String.format("%.3fGs", i16_val/1000.0))
        i16_val = ((byteArray[17].toInt() and 0xff shl 8) + (byteArray[16].toInt() and 0xff)).toShort()
        mDatas.set(10, String.format("%.3fGs", i16_val/1000.0))
        i16_val = ((byteArray[19].toInt() and 0xff shl 8) + (byteArray[18].toInt() and 0xff)).toShort()
        mDatas.set(11, String.format("%.3fGs", i16_val/1000.0))

        i16_val = ( (byteArray[21].toInt() and 0xff shl 8) + (byteArray[20].toInt() and 0xff)).toShort()
        mDatas.set(3, String.format("%.3fg", i16_val/1000.0))
        i16_val = ((byteArray[23].toInt() and 0xff shl 8) + (byteArray[22].toInt() and 0xff)).toShort()
        mDatas.set(4, String.format("%.3fGs", i16_val/1000.0))
        i16_val = ((byteArray[25].toInt() and 0xff shl 8) + (byteArray[24].toInt() and 0xff)).toShort()
        mDatas.set(5, String.format("%.1f°", i16_val/10.0))

        i16_val = ( (byteArray[27].toInt() and 0xff shl 8) + (byteArray[26].toInt() and 0xff)).toShort()
        mDatas.set(0, String.format("%.1f°", i16_val/10.0))
        i16_val = ((byteArray[29].toInt() and 0xff shl 8) + (byteArray[28].toInt() and 0xff)).toShort()
        mDatas.set(1, String.format("%.1f°", i16_val/10.0))
        i16_val = ((byteArray[31].toInt() and 0xff shl 8) + (byteArray[30].toInt() and 0xff)).toShort()
        mDatas.set(2, String.format("%.1f°", i16_val/10.0))

        i16_val = ((byteArray[33].toInt() and 0xff shl 8) + (byteArray[32].toInt() and 0xff)).toShort()
        mDatas.set(12, String.format("%.2fV", i16_val/100.0))

        adapter.notifyDataSetChanged()
    }

    inner class mAdapter : RecyclerView.Adapter<mAdapter.MyViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            return MyViewHolder(
                LayoutInflater.from(parent.getContext ()).inflate(R.layout.item_value, parent, false
                )
            )
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            holder.tv_name.setText(mDatas_name.get(position))
            holder.tv_content.setText(mDatas.get(position))
        }

        override fun getItemCount(): Int {
            return mDatas.size
        }

        inner class MyViewHolder(view: View) : ViewHolder(view) {
            var tv_name:TextView
            var tv_content: TextView

            init {
                tv_name  = view.findViewById<View>(R.id.rec_val_name) as TextView
                tv_content = view.findViewById<View>(R.id.rec_val_content) as TextView
            }
        }
    }

}
