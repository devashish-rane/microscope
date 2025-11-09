package com.example.orders.order;

import com.example.orders.sql.SqlCaptureTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderRepo {
    private final SqlCaptureTemplate sql;

    public OrderRepo(SqlCaptureTemplate sql) {
        this.sql = sql;
    }

    public void initSchema() {
        sql.update("CREATE TABLE IF NOT EXISTS orders (id SERIAL PRIMARY KEY, user_id BIGINT, item TEXT, amount INT)");
        sql.update("INSERT INTO orders(user_id,item,amount) VALUES (?,?,?)", 1, "book", 1);
        sql.update("INSERT INTO orders(user_id,item,amount) VALUES (?,?,?)", 1, "pen", 3);
        sql.update("INSERT INTO orders(user_id,item,amount) VALUES (?,?,?)", 2, "bag", 1);
    }

    public List<OrderRow> findByUser(long userId) {
        return sql.query("SELECT id,user_id,item,amount FROM orders WHERE user_id=?",
                (rs, i) -> new OrderRow(rs.getLong(1), rs.getLong(2), rs.getString(3), rs.getInt(4)), userId);
    }
}

