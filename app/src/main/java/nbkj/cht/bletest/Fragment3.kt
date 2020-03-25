package nbkj.cht.bletest

import android.graphics.Point
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast


class Fragment3 : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment3, container, false)
    }
    override fun onStart() {
        super.onStart()

        val btn = this.view!!.findViewById<Button>(R.id.btn_Exit)
        btn.setOnClickListener(MyButtonListener())
    }



    private inner class MyButtonListener : View.OnClickListener {


        override fun onClick(view: View) {

            val fm = activity?.getSupportFragmentManager();
            val ft = fm!!.beginTransaction();

            ft.hide(MainActivity.f0);
            ft.hide(MainActivity.f1);
            ft.hide(MainActivity.f2);
            ft.show(MainActivity.f4);
            ft.hide(MainActivity.f3);
            ft.commit()
        }
    }
}
