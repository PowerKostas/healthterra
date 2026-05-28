package com.kostas.gohealth.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
Python code to create the item_size_calories.db file
import pandas as pd
import sqlite3

df = pd.read_csv('C:/Users/Kostas/Downloads/item_size_calories.csv')

db_path = 'C:/Users/Kostas/Downloads/item_size_calories.db'
conn = sqlite3.connect(db_path)
cursor = conn.cursor()

cursor.execute('''
    CREATE TABLE IF NOT EXISTS food (
        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
        item TEXT NOT NULL,
        size TEXT NOT NULL,
        calories INTEGER NOT NULL
    )
''')

df[['Item', 'Size', 'Calories']].to_sql('food', conn, if_exists='append', index=False)

conn.commit()
conn.close()

*/

@Entity(tableName = "food")
data class Food(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "item") val item: String,
    @ColumnInfo(name = "size") val size: String,
    @ColumnInfo(name = "calories") val calories: Int
)
