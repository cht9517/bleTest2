package nbkj.cht.bletest

import android.graphics.Color
import android.graphics.Point
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.LinearLayoutManager

import android.widget.TextView
import android.support.v7.widget.RecyclerView.ViewHolder
import android.widget.Button
import android.widget.Toast
import java.util.*
import java.io.File
import nbkj.cht.bletest.Fragment4.OnRecyclerViewClickListener




class Fragment4 : Fragment() {

    val adapter = mAdapter()
    var MyFileList = ArrayList<MyFileItem>();

    var file_index = 0;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_fragment4, container, false)

        val mRecyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        mRecyclerView.setLayoutManager(LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false))


        mRecyclerView.setAdapter(adapter)

        adapter.setItemClickListener(object : OnRecyclerViewClickListener {
            override fun onItemClickListener(v : View) {

                var position = mRecyclerView.getChildAdapterPosition(v);
                adapter.file_pos_set(position);
                file_index = position;

                adapter.notifyDataSetChanged()
                }
            });

        var Draw_Btn = view.findViewById<Button>(R.id.btn_Draw);
        Draw_Btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {

                val dir_str = Environment.getExternalStorageDirectory().absolutePath + "/BLE-MWD"
                val file_sel = File(dir_str + "/" + MyFileList.get(file_index).fileName);

                val textBox_base = activity!!.findViewById<TextView>(R.id.editText_base);


                val ldr = LogDataRead(file_sel);

                var str = textBox_base.text.toString();
                val base = java.lang.Double.parseDouble(str) * 10;
                val num = ldr._num;


                val vx = ldr._Vx;
                val vy = ldr._Vy;

                var graph = activity!!.findViewById<MyGraph>(R.id.graph_dft)

                graph.clear();

                for(i in 0..(num - 1) ) {
                    if(graph.points[i] == null) {
                        graph.points[i] = Point((vx[i]*base).toInt(), (vy[i]*base).toInt());
                    }
                    else {
                        graph.points[i].x = (vx[i]*base).toInt();
                        graph.points[i].y = (vy[i]*base).toInt();
                    }

                }
                graph.invalidate()

                //Toast.makeText(activity, ldr._AngleH.toString(), Toast.LENGTH_SHORT).show()


                val hx = ldr._Hx;
                val hy = ldr._Hy;

                graph = activity!!.findViewById<MyGraphAzim>(R.id.graph_azim)
                graph.clear();

                for(i in 0..(num - 1) ) {
                    if(graph.points[i] == null) {
                        graph.points[i] = Point((hx[i]*base).toInt(), (hy[i]*base).toInt());
                    }
                    else {
                        graph.points[i].x = (hx[i]*base).toInt();
                        graph.points[i].y = (hy[i]*base).toInt();
                    }

                }
                graph.Base_Angle = ldr._AngleHBase;
                graph.invalidate()

                //Toast.makeText(activity, ldr._AngleHBase.toString(), Toast.LENGTH_SHORT).show()

                val fm = activity?.getSupportFragmentManager();
                val ft = fm!!.beginTransaction();

                ft.hide(MainActivity.f0);
                ft.hide(MainActivity.f1);
                ft.hide(MainActivity.f2);
                ft.show(MainActivity.f3);
                ft.hide(MainActivity.f4);
                ft.commit()
            }
        })

        return view;
    }

    fun read_log_data(file: File)
    {

    }

    fun get_mAdapter():mAdapter{
        return adapter;
    }

    fun update_fileItemList()
    {
        MyFileList.clear()

        val dir_str = Environment.getExternalStorageDirectory().absolutePath + "/BLE-MWD"
        val file_Dir:File = File(dir_str)
        val files = ArrayList<File>()
        files.addAll(file_Dir.listFiles())

        Collections.sort(files,Collections.reverseOrder());

        for (file in files)
        {
            var fileName = file.getName();
            val fileType = fileName.substring(fileName.lastIndexOf("."), fileName.length)

            if(fileType.equals(".dld")) {
                var fileSize = file.length();

                var fileItem = MyFileItem(fileName, fileSize.toString());
                MyFileList.add(fileItem);
            }
        }

        adapter.notifyDataSetChanged()
    }

    interface OnRecyclerViewClickListener {
        fun onItemClickListener(view: View)
    }

    inner class mAdapter : RecyclerView.Adapter<mAdapter.MyViewHolder>() {

        private var listener: OnRecyclerViewClickListener? = null

        var file_pos = 0;// 默认等于0 则默认选中第一项 默认等于<0 则默认不选中

        fun setItemClickListener(itemClickListener: OnRecyclerViewClickListener)
        {
            listener = itemClickListener
        }

        fun file_pos_set(pos: Int)
        {
            file_pos = pos;
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

            val view = LayoutInflater.from(parent.getContext ()).inflate(R.layout.file_item, parent, false);
            val viewHolder = MyViewHolder(view);

            //接口回调
            if (listener != null) {
                view.setOnClickListener(object : View.OnClickListener {
                    override fun onClick(v: View) {
                        listener!!.onItemClickListener(v);
                    }
                })
            }

            return viewHolder;
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val myFile = MyFileList.get(position)
            holder.name.setText(myFile.getFileName())
            holder.size.setText(myFile.getFileSize())

            if (file_pos == position){
                holder.itemView.setBackgroundColor(Color.parseColor("#8080ff"));
            }else {
                holder.itemView.setBackgroundColor(Color.parseColor("#e2ede2"));
            }

        }

        override fun getItemCount(): Int {
            return MyFileList.size
        }

        inner class MyViewHolder(view: View) : ViewHolder(view) {
            var name:TextView
            var size: TextView

            init {
                name  = view.findViewById<View>(R.id.file_name) as TextView
                size = view.findViewById<View>(R.id.file_size) as TextView
            }
        }
    }

}
