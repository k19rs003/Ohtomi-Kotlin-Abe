package jp.ac.kyusanu.ohtomi

object HubenyDistance {

    //世界観測値系
    val GRS80_A = 6378137.000//長半径 a(m)
    val GRS80_E2 = 0.00669438002301188//第一遠心率  eの2乗

    fun deg2rad(deg: Double): Double {
        return deg * Math.PI / 180.0
    }

    fun calcDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val my = deg2rad((lat1 + lat2) / 2.0) //緯度の平均値
        val dy = deg2rad(lat1 - lat2) //緯度の差
        val dx = deg2rad(lng1 - lng2) //経度の差

        //卯酉線曲率半径を求める(東と西を結ぶ線の半径)
        val sinMy = Math.sin(my)
        val w = Math.sqrt(1.0 - GRS80_E2 * sinMy * sinMy)
        val n = GRS80_A / w

        //子午線曲線半径を求める(北と南を結ぶ線の半径)
        val mnum = GRS80_A * (1 - GRS80_E2)
        val m = mnum / (w * w * w)

        //ヒュベニの公式
        val dym = dy * m
        val dxncos = dx * n * Math.cos(my)
        return Math.sqrt(dym * dym + dxncos * dxncos)
    }
}
