### Task 1

For the sake of simplicity we will use 1 Zookeeper, 1 JournalNode, 2 NameNodes and 1 DataNode to be able to run on a single Docker host. It goes without saying that you should adjust these numbers in production.

- Create a common docker network

```docker network create hadoop```

- Start Zookeeper

```docker run --net=hadoop --name zk-1 --restart always -d zookeeper```

- Start JournalNode

```docker run -d --name=jn-1 -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-jn:/mnt/hadoop artkostm/hadoop-ha /etc/bootstrap.sh -d journalnode```

- Format the active NameNode

```docker run --hostname=nn1 --name=nn1 -it -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-nn1:/mnt/hadoop artkostm/hadoop-ha /etc/bootstrap.sh -d format```

- Sync the initial state to the standby NameNode

```docker run --hostname=nn2 --name=nn2 -it -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-nn2:/mnt/hadoop -v /tmp/hadoop-nn1:/mnt/shared/nn1 artkostm/hadoop-ha /etc/bootstrap.sh -d standby```
<br>Notice that the volume from nn1 - which now holds the initial cluster state - is just mounted to a certain directory where all data will be copied to nn2's volume.
At this point both volumes hold the initial cluster state and can be used as a mountpoint in actual NameNode images.

- Start both NameNodes (separate terminals)
```docker run --hostname=nn1 -p 50060:50070 --name=nn1 -it -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-nn1:/mnt/hadoop artkostm/hadoop-ha /etc/bootstrap.sh -d namenode```
```docker run --hostname=nn2 --name=nn2 -p 50070:50070 -it -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-nn2:/mnt/hadoop artkostm/hadoop-ha /etc/bootstrap.sh -d namenode```
<br>Now both NameNodes should be running, check it by visiting the WebUI on Port 50060 (nn1) and 50070 (nn2). nn2 should be standby while nn1 is active.

- Start DataNodes

```docker run -d -e "NNODE1_IP=nn1" -e "NNODE2_IP=nn2" -e "JN_IPS=jn-1:8485" -e "ZK_IPS=zk-1:2181" --net=hadoop -v /tmp/hadoop-dn-1:/mnt/hadoop artkostm/hadoop-ha /etc/bootstrap.sh -d datanode```

- Kill the active NameNode to trigger failover.
Just press CTRL-C on the terminal which is attached to the active NameNode. Now watch on the WebUI how the standby NameNode gets active.
DataNodes are still connected. Wait a bit and restart the formerly active NameNode. Now it will be the standby Node.
