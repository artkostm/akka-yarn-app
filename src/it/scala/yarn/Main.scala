package by.artsiom.bigdata201.yarn

//import com.github.sakserv.minicluster.impl.HdfsLocalCluster
//import org.apache.hadoop.conf.Configuration
//import org.scalatest.FlatSpec
//
//class TestHdfsUtility extends FlatSpec  {
//
//  val hdfsLocalCluster = new HdfsLocalCluster.Builder()
//    .setHdfsNamenodePort(12345)
//    .setHdfsNamenodeHttpPort(12341)
//    .setHdfsTempDir("embedded_hdfs")
//    .setHdfsNumDatanodes(1)
//    .setHdfsEnablePermissions(false)
//    .setHdfsFormat(true)
//    .setHdfsEnableRunningUserAsProxyUser(true)
//    .setHdfsConfig(new Configuration())
//    .build()
//
//  "This test" should "start/stop mincluster" in {
//    hdfsLocalCluster.start()
//    hdfsLocalCluster.stop()
//  }
//}
