-- 历史消息
CREATE TABLE IF NOT EXISTS group_messages (
    id SERIAL PRIMARY KEY,
    group_id INT8 NOT NULL,
    user_id INT8 NOT NULL,
    username VARCHAR(24) NOT NULL,
    message TEXT NOT NULL
);