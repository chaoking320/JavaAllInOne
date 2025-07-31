# JavaAllInOne

所有跟java相关的一切，也算对自己程序生涯的一次深情告白。

## redis相关
### docker 安装redis 
```bash
# 暴露6379端口，这样127.0.0.1:6379 可连接
docker run -d --name my-redis -p 6379:6379 redis

# 运行docker中的 redis-cli 客户端
docker exec -it my-redis redis-cli
```