# JavaAllInOne

所有跟java相关的一切，也算对自己程序生涯的一次深情告白。

## redis相关
### docker 安装redis 
```bash
# 暴露6379端口，这样127.0.0.1:6379 可连接 映射数据文件，映射redis.conf文件（有些参数无法通过config set param 修改）
docker run -d --name myredis   -v D:\redisdata:/data   -v D:\redisdata\redis.conf:/usr/local/etc/redis/redis.conf -p 6379:6379 redis:7.2.4 redis-server /usr/local/etc/redis/redis.conf

# 运行docker中的 redis-cli 客户端
docker exec -it my-redis redis-cli
```

### 分布式锁
    private final static String RELEASE_LOCK_LUA =
            "if redis.call('get',KEYS[1])==ARGV[1] then\n" +
                    "        return redis.call('del', KEYS[1])\n" +
                    "    else return 0 end";
参见 RedisDistLock

自动续期（看门狗） 参见  RedisDistLockWithDog