-- =============================================
-- 补充其他分类的商品测试数据
-- =============================================

-- ========== 1. 电脑办公 (340005152813748224) ==========
INSERT INTO product_spu (id, category_id, name, description, brand, main_image, images, status, create_time, update_time)
VALUES
(341000000000000001, 340005152813748224, 'ThinkPad X1 Carbon', '联想旗舰商务笔记本，轻薄长续航', '联想', NULL, NULL, 1, NOW(), NOW()),
(341000000000000002, 340005152813748224, 'iPad Air', 'Apple iPad Air M2芯片 11英寸', 'Apple', NULL, NULL, 1, NOW(), NOW()),
(341000000000000003, 340005152813748224, '机械革命 蛟龙16', '16英寸游戏本 RTX4060', '机械革命', NULL, NULL, 1, NOW(), NOW());

INSERT INTO product_sku (id, spu_id, name, sku_code, price, stock, images, specs, status, create_time, update_time)
VALUES
(341000000000100001, 341000000000000001, 'ThinkPad X1 Carbon-i7/16G/512G', 'TP-X1C-001', 8999.00, 50, NULL, '{"color":"黑色","cpu":"i7-1365U"}', 1, NOW(), NOW()),
(341000000000100002, 341000000000000001, 'ThinkPad X1 Carbon-i7/32G/1TB', 'TP-X1C-002', 10999.00, 30, NULL, '{"color":"黑色","cpu":"i7-1370P"}', 1, NOW(), NOW()),
(341000000000100003, 341000000000000002, 'iPad Air-64GB WiFi', 'IPA-AIR-001', 4799.00, 80, NULL, '{"color":"深空灰","storage":"64GB"}', 1, NOW(), NOW()),
(341000000000100004, 341000000000000002, 'iPad Air-256GB WiFi', 'IPA-AIR-002', 5999.00, 60, NULL, '{"color":"星光色","storage":"256GB"}', 1, NOW(), NOW()),
(341000000000100005, 341000000000000003, '机械革命 蛟龙16-R7/16G/512G', 'MR-JL16-001', 6499.00, 40, NULL, '{"color":"灰色","gpu":"RTX4060"}', 1, NOW(), NOW()),
(341000000000100006, 341000000000000003, '机械革命 蛟龙16-R9/32G/1TB', 'MR-JL16-002', 8499.00, 20, NULL, '{"color":"灰色","gpu":"RTX4070"}', 1, NOW(), NOW());

INSERT INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock, create_time, update_time)
VALUES
(341000000000200001, 341000000000100001, 50, 0, 50, 5, NOW(), NOW()),
(341000000000200002, 341000000000100002, 30, 0, 30, 5, NOW(), NOW()),
(341000000000200003, 341000000000100003, 80, 0, 80, 10, NOW(), NOW()),
(341000000000200004, 341000000000100004, 60, 0, 60, 10, NOW(), NOW()),
(341000000000200005, 341000000000100005, 40, 0, 40, 5, NOW(), NOW()),
(341000000000200006, 341000000000100006, 20, 0, 20, 5, NOW(), NOW());

-- ========== 2. 服装鞋帽 (340005153149292544) ==========
INSERT INTO product_spu (id, category_id, name, description, brand, main_image, images, status, create_time, update_time)
VALUES
(341000000000000004, 340005153149292544, '纯棉圆领T恤', '优衣库基础款纯棉T恤，舒适透气', '优衣库', NULL, NULL, 1, NOW(), NOW()),
(341000000000000005, 340005153149292544, 'Air Max 270 休闲运动鞋', 'Nike经典气垫运动鞋', 'Nike', NULL, NULL, 1, NOW(), NOW()),
(341000000000000006, 340005153149292544, '免烫商务衬衫', '海澜之家免烫修身长袖衬衫', '海澜之家', NULL, NULL, 1, NOW(), NOW());

INSERT INTO product_sku (id, spu_id, name, sku_code, price, stock, images, specs, status, create_time, update_time)
VALUES
(341000000000100007, 341000000000000004, '纯棉圆领T恤-S码-白色', 'UQ-TEE-S-WH', 99.00, 200, NULL, '{"color":"白色","size":"S"}', 1, NOW(), NOW()),
(341000000000100008, 341000000000000004, '纯棉圆领T恤-M码-黑色', 'UQ-TEE-M-BK', 99.00, 200, NULL, '{"color":"黑色","size":"M"}', 1, NOW(), NOW()),
(341000000000100009, 341000000000000005, 'Air Max 270-42码-黑白', 'NK-AM270-42-BW', 899.00, 100, NULL, '{"color":"黑白","size":"42"}', 1, NOW(), NOW()),
(341000000000100010, 341000000000000005, 'Air Max 270-43码-纯白', 'NK-AM270-43-WH', 899.00, 80, NULL, '{"color":"纯白","size":"43"}', 1, NOW(), NOW()),
(341000000000100011, 341000000000000006, '免烫商务衬衫-39码-浅蓝', 'HLZS-CS-39-BL', 299.00, 150, NULL, '{"color":"浅蓝","size":"39"}', 1, NOW(), NOW()),
(341000000000100012, 341000000000000006, '免烫商务衬衫-40码-白色', 'HLZS-CS-40-WH', 299.00, 120, NULL, '{"color":"白色","size":"40"}', 1, NOW(), NOW());

INSERT INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock, create_time, update_time)
VALUES
(341000000000200007, 341000000000100007, 200, 0, 200, 20, NOW(), NOW()),
(341000000000200008, 341000000000100008, 200, 0, 200, 20, NOW(), NOW()),
(341000000000200009, 341000000000100009, 100, 0, 100, 10, NOW(), NOW()),
(341000000000200010, 341000000000100010, 80, 0, 80, 10, NOW(), NOW()),
(341000000000200011, 341000000000100011, 150, 0, 150, 15, NOW(), NOW()),
(341000000000200012, 341000000000100012, 120, 0, 120, 15, NOW(), NOW());

-- ========== 3. 家用电器 (340005153442893824) ==========
INSERT INTO product_spu (id, category_id, name, description, brand, main_image, images, status, create_time, update_time)
VALUES
(341000000000000007, 340005153442893824, '变频冷暖空调 1.5匹', '格力新一级能效变频空调', '格力', NULL, NULL, 1, NOW(), NOW()),
(341000000000000008, 340005153442893824, '全自动滚筒洗衣机 10kg', '海尔变频静音滚筒洗衣机', '海尔', NULL, NULL, 1, NOW(), NOW()),
(341000000000000009, 340005153442893824, '对开门冰箱 520L', '美的风冷无霜对开门冰箱', '美的', NULL, NULL, 1, NOW(), NOW());

INSERT INTO product_sku (id, spu_id, name, sku_code, price, stock, images, specs, status, create_time, update_time)
VALUES
(341000000000100013, 341000000000000007, '格力空调 1.5匹-挂机', 'GL-AC-1.5-WALL', 3299.00, 50, NULL, '{"type":"挂机","p":"1.5匹"}', 1, NOW(), NOW()),
(341000000000100014, 341000000000000007, '格力空调 2匹-柜机', 'GL-AC-2.0-CAB', 5999.00, 30, NULL, '{"type":"柜机","p":"2匹"}', 1, NOW(), NOW()),
(341000000000100015, 341000000000000008, '海尔洗衣机 10kg-银色', 'HR-WM-10KG-SL', 2499.00, 60, NULL, '{"color":"银色","capacity":"10kg"}', 1, NOW(), NOW()),
(341000000000100016, 341000000000000008, '海尔洗衣机 12kg-星蕴银', 'HR-WM-12KG-SY', 3299.00, 40, NULL, '{"color":"星蕴银","capacity":"12kg"}', 1, NOW(), NOW()),
(341000000000100017, 341000000000000009, '美的冰箱 520L-白色', 'MD-FG-520-WH', 3499.00, 50, NULL, '{"color":"白色","capacity":"520L"}', 1, NOW(), NOW()),
(341000000000100018, 341000000000000009, '美的冰箱 600L-星耀灰', 'MD-FG-600-GY', 4599.00, 30, NULL, '{"color":"星耀灰","capacity":"600L"}', 1, NOW(), NOW());

INSERT INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock, create_time, update_time)
VALUES
(341000000000200013, 341000000000100013, 50, 0, 50, 5, NOW(), NOW()),
(341000000000200014, 341000000000100014, 30, 0, 30, 5, NOW(), NOW()),
(341000000000200015, 341000000000100015, 60, 0, 60, 10, NOW(), NOW()),
(341000000000200016, 341000000000100016, 40, 0, 40, 5, NOW(), NOW()),
(341000000000200017, 341000000000100017, 50, 0, 50, 5, NOW(), NOW()),
(341000000000200018, 341000000000100018, 30, 0, 30, 5, NOW(), NOW());

-- ========== 4. 运动户外 (340005153732300800) ==========
INSERT INTO product_spu (id, category_id, name, description, brand, main_image, images, status, create_time, update_time)
VALUES
(341000000000000010, 340005153732300800, '户外登山背包 45L', '探路者专业登山包，防水耐磨', '探路者', NULL, NULL, 1, NOW(), NOW()),
(341000000000000011, 340005153732300800, '加厚防滑瑜伽垫', 'Keep NBR材质瑜伽垫 10mm', 'Keep', NULL, NULL, 1, NOW(), NOW()),
(341000000000000012, 340005153732300800, '27速山地自行车', '捷安特铝合金车架 27速', '捷安特', NULL, NULL, 1, NOW(), NOW());

INSERT INTO product_sku (id, spu_id, name, sku_code, price, stock, images, specs, status, create_time, update_time)
VALUES
(341000000000100019, 341000000000000010, '登山背包 45L-蓝色', 'TLZ-BP-45-BL', 399.00, 80, NULL, '{"color":"蓝色","capacity":"45L"}', 1, NOW(), NOW()),
(341000000000100020, 341000000000000010, '登山背包 45L-军绿色', 'TLZ-BP-45-GR', 399.00, 60, NULL, '{"color":"军绿","capacity":"45L"}', 1, NOW(), NOW()),
(341000000000100021, 341000000000000011, '瑜伽垫 10mm-紫色', 'KP-YG-10MM-PU', 129.00, 300, NULL, '{"color":"紫色","thickness":"10mm"}', 1, NOW(), NOW()),
(341000000000100022, 341000000000000011, '瑜伽垫 15mm-灰色', 'KP-YG-15MM-GY', 169.00, 200, NULL, '{"color":"灰色","thickness":"15mm"}', 1, NOW(), NOW()),
(341000000000100023, 341000000000000012, '山地自行车 27速-26寸-黑红', 'GAT-MTB-27-26-RD', 2499.00, 30, NULL, '{"color":"黑红","size":"26寸"}', 1, NOW(), NOW()),
(341000000000100024, 341000000000000012, '山地自行车 27速-27.5寸-消光灰', 'GAT-MTB-27-27.5-GY', 2999.00, 20, NULL, '{"color":"消光灰","size":"27.5寸"}', 1, NOW(), NOW());

INSERT INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock, create_time, update_time)
VALUES
(341000000000200019, 341000000000100019, 80, 0, 80, 10, NOW(), NOW()),
(341000000000200020, 341000000000100020, 60, 0, 60, 10, NOW(), NOW()),
(341000000000200021, 341000000000100021, 300, 0, 300, 30, NOW(), NOW()),
(341000000000200022, 341000000000100022, 200, 0, 200, 20, NOW(), NOW()),
(341000000000200023, 341000000000100023, 30, 0, 30, 5, NOW(), NOW()),
(341000000000200024, 341000000000100024, 20, 0, 20, 5, NOW(), NOW());

-- ========== 5. 食品饮料 (340005154017513472) ==========
INSERT INTO product_spu (id, category_id, name, description, brand, main_image, images, status, create_time, update_time)
VALUES
(341000000000000013, 340005154017513472, '每日坚果礼盒 750g', '三只松鼠混合坚果30袋装', '三只松鼠', NULL, NULL, 1, NOW(), NOW()),
(341000000000000014, 340005154017513472, '特仑苏纯牛奶 250ml*12盒', '蒙牛高端纯牛奶', '蒙牛', NULL, NULL, 1, NOW(), NOW()),
(341000000000000015, 340005154017513472, '明前龙井绿茶 250g', '张一元特级明前龙井', '张一元', NULL, NULL, 1, NOW(), NOW());

INSERT INTO product_sku (id, spu_id, name, sku_code, price, stock, images, specs, status, create_time, update_time)
VALUES
(341000000000100025, 341000000000000013, '每日坚果礼盒 750g-经典款', 'SSS-NUT-750-CL', 89.00, 500, NULL, '{"spec":"经典款","weight":"750g"}', 1, NOW(), NOW()),
(341000000000100026, 341000000000000013, '每日坚果礼盒 1kg-升级款', 'SSS-NUT-1KG-UP', 118.00, 300, NULL, '{"spec":"升级款","weight":"1kg"}', 1, NOW(), NOW()),
(341000000000100027, 341000000000000014, '特仑苏纯牛奶 250ml*12盒', 'MN-TLS-250*12', 59.00, 800, NULL, '{"spec":"250ml*12","type":"全脂"}', 1, NOW(), NOW()),
(341000000000100028, 341000000000000014, '特仑苏低脂牛奶 250ml*12盒', 'MN-TLS-LF-250*12', 59.00, 500, NULL, '{"spec":"250ml*12","type":"低脂"}', 1, NOW(), NOW()),
(341000000000100029, 341000000000000015, '明前龙井 250g-特级', 'ZYY-LJ-250G-SP', 268.00, 100, NULL, '{"spec":"特级","weight":"250g"}', 1, NOW(), NOW()),
(341000000000100030, 341000000000000015, '明前龙井 500g-一级', 'ZYY-LJ-500G-FST', 428.00, 80, NULL, '{"spec":"一级","weight":"500g"}', 1, NOW(), NOW());

INSERT INTO inventory (id, sku_id, total_stock, locked_stock, available_stock, safety_stock, create_time, update_time)
VALUES
(341000000000200025, 341000000000100025, 500, 0, 500, 50, NOW(), NOW()),
(341000000000200026, 341000000000100026, 300, 0, 300, 30, NOW(), NOW()),
(341000000000200027, 341000000000100027, 800, 0, 800, 100, NOW(), NOW()),
(341000000000200028, 341000000000100028, 500, 0, 500, 50, NOW(), NOW()),
(341000000000200029, 341000000000100029, 100, 0, 100, 10, NOW(), NOW()),
(341000000000200030, 341000000000100030, 80, 0, 80, 10, NOW(), NOW());
