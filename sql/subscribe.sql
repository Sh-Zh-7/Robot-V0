-- B站订阅主播
CREATE TABLE IF NOT EXISTS group_subscribed_bv_streamer(
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    user_id INT8 NOT NULL,
    live_id INT8 NOT NULL
);

-- B站用户动态
CREATE TABLE IF NOT EXISTS group_subscribed_bv_user(
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    user_id INT8 NOT NULL,
    published DATE NOT NULL
);

-- Github订阅用户
CREATE TABLE IF NOT EXISTS group_subscribed_github(
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    username VARCHAR(40) NOT NULL,
    published DATE NOT NULL
);

-- 微博订阅用户
CREATE TABLE IF NOT EXISTS group_subscribed_weibo(
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    weibo_id INT8 NOT NULL,
    username VARCHAR(32) NOT NULL,
    published DATE
);

-- 知乎订阅用户
CREATE TABLE IF NOT EXISTS group_subscribed_zhihu(
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    username VARCHAR(64) NOT NULL,
    answer DATE,
    post DATE,
    pin DATE
);
