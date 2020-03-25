package nbkj.cht.bletest

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.clj.fastble.BleManager


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [Fragment0.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [Fragment0.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class Fragment0 : Fragment() {

    var textViewConnect: TextView? = null
    var btnReconnect:Button? = null

    fun textViewConnectSet(txt:String)
    {
        textViewConnect?.setText(txt);
    }

    fun btnReconnectShow(Visi:Int)
    {
        btnReconnect?.visibility = Visi;
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_fragment0, container, false)

        textViewConnect =  view.findViewById<TextView>(R.id.textViewConnect);
        textViewConnectSet("连接状态：0");

        btnReconnect =  view.findViewById<Button>(R.id.btnReconnect);
        btnReconnect?.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                MainActivity.ble_ops.checkPermissions();
            }
        })

        return view;
    }

}
