package com.example.webservice

import android.net.Uri
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    val urlGetData : String  = "http://10.12.180.119/webservice/getdata.php"
    val urlInsertData : String  = "http://10.12.180.119/webservice/insertdata.php"

    var mangKH : ArrayList<String> = ArrayList()
    var adapterKH : ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        GetData().execute(urlGetData)

        adapterKH = ArrayAdapter(this, android.R.layout.simple_list_item_1, mangKH)
        lv.adapter = adapterKH

        btnThem.setOnClickListener {
            them()
        }


    }

    private fun them() {
        val ten : String = edtTen.text.toString().trim()
        val hp : String = edtHocPhi.text.toString().trim()
        if (ten.length == 0 || hp.length == 0){
            Toast.makeText(applicationContext, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
        }else{
            InsertData().execute(urlInsertData)
            edtHocPhi.setText("")
            edtTen.setText("")
            GetData().execute(urlGetData)

        }
    }

    inner class GetData : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
                return getContentURL(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
//            Toast.makeText(applicationContext, result, Toast.LENGTH_LONG ).show()
            var jsonArray : JSONArray = JSONArray(result)

            var ten : String = ""
            var hp : String = ""
            mangKH.clear()

            for (khoaHoc in 0..jsonArray.length() -1){
                var objectKH : JSONObject = jsonArray.getJSONObject(khoaHoc)
                ten = objectKH.getString("TenKH")
                hp = objectKH.getString("HocPhi")
                mangKH.add(ten+ "-" + hp)

            }
            adapterKH?.notifyDataSetChanged()
        }
        private fun getContentURL(url: String?) : String{
            var content: StringBuilder = StringBuilder()
            val url: URL = URL(url)
            val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
            val inputStreamReader: InputStreamReader = InputStreamReader(urlConnection.inputStream)
            val bufferedReader: BufferedReader = BufferedReader(inputStreamReader)

            var line: String = ""
            try {
                do {
                    line = bufferedReader.readLine()
                    if(line != null){
                        content.append(line)
                    }
                }while (line != null)
                bufferedReader.close()
            }catch (e: Exception){
                Log.d("AAA", e.toString())
            }
            return content.toString()
        }
    }
    inner class InsertData : AsyncTask<String, Void, String>(){
        override fun doInBackground(vararg params: String?): String {
            return postData(params[0])
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if(result.equals("success")){
                Toast.makeText(applicationContext, "Thêm thành công", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(applicationContext, "Thêm thất bại", Toast.LENGTH_SHORT).show()

            }
        }
        private fun postData(link: String?): String {
            val connect: HttpURLConnection
            var url: URL =  URL(link)
            try {
                connect = url.openConnection() as HttpURLConnection
                connect.readTimeout = 10000
                connect.connectTimeout = 15000
                connect.requestMethod = "POST"
                // POST theo tham số
                val builder = Uri.Builder()
                    .appendQueryParameter("tenkhoahoc", edtTen.text.toString().trim())
                    .appendQueryParameter("hocphiKH", edtHocPhi.text.toString().trim())
                val query = builder.build().getEncodedQuery()
                val os = connect.outputStream
                val writer = BufferedWriter(OutputStreamWriter(os, "UTF-8"))
                writer.write(query)
                writer.flush()
                writer.close()
                os.close()
                connect.connect()
            } catch (e1: IOException) {
                e1.printStackTrace()
                return "Error!"
            }

            try {
                // Đọc nội dung trả về sau khi thực hiện POST
                val response_code = connect.responseCode
                if (response_code == HttpURLConnection.HTTP_OK) {
                    val input = connect.inputStream
                    val reader = BufferedReader(InputStreamReader(input))
                    val result = StringBuilder()
                    var line: String
                    try {
                        do{
                            line = reader.readLine()
                            if(line != null){
                                result.append(line)
                            }
                        }while (line != null)

                        reader.close()
                    }catch (e:Exception){}

                    return result.toString()
                } else {
                    return "Error!"
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return "Error!"
            } finally {
                connect.disconnect()
            }
        }
    }

}
