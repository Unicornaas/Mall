package edu.fjut.mall.cart.service.impl;

import edu.fjut.mall.cart.dto.CartAddRequest;
import edu.fjut.mall.cart.dto.CartVO;
import edu.fjut.mall.cart.entity.Cart;
import edu.fjut.mall.cart.mapper.CartMapper;
import edu.fjut.mall.cart.service.CartService;
import edu.fjut.mall.common.exception.BusinessException;
import edu.fjut.mall.common.result.ResultCode;
import edu.fjut.mall.common.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final SnowflakeIdGenerator idGen;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public CartVO add(CartAddRequest request) {
        // 1. 校验SKU是否存在
        Map<String, Object> sku;
        try {
            sku = jdbcTemplate.queryForMap(
                "SELECT id, spu_id, name, price, images FROM product_sku WHERE id = ?", request.getSkuId());
        } catch (Exception e) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "商品SKU不存在");
        }

        // 2. 检查是否已存在同一SKU
        Cart existing = cartMapper.selectByUserAndSku(request.getUserId(), request.getSkuId());
        if (existing != null) {
            // 已存在则数量累加
            int newQuantity = existing.getQuantity() + request.getQuantity();
            cartMapper.updateQuantity(existing.getId(), newQuantity);
            log.info("购物车商品数量累加: cartId={}, skuId={}, newQuantity={}", existing.getId(), request.getSkuId(), newQuantity);
            existing.setQuantity(newQuantity);
            return toVO(existing, sku);
        }

        // 3. 新增购物车项
        Cart cart = new Cart();
        cart.setId(idGen.nextId());
        cart.setUserId(request.getUserId());
        cart.setSkuId(request.getSkuId());
        cart.setQuantity(request.getQuantity());
        cart.setSelected(1);  // 默认选中
        cart.setCreateTime(LocalDateTime.now());
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.insert(cart);

        log.info("购物车添加成功: userId={}, skuId={}, quantity={}", request.getUserId(), request.getSkuId(), request.getQuantity());
        return toVO(cart, sku);
    }

    @Override
    public List<CartVO> list(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        List<CartVO> vos = new ArrayList<>();
        for (Cart cart : carts) {
            try {
                Map<String, Object> sku = jdbcTemplate.queryForMap(
                    "SELECT id, spu_id, name, price, images FROM product_sku WHERE id = ?", cart.getSkuId());
                vos.add(toVO(cart, sku));
            } catch (Exception e) {
                log.warn("购物车中SKU {} 已失效，跳过", cart.getSkuId());
            }
        }
        return vos;
    }

    @Override
    @Transactional
    public CartVO updateQuantity(Long id, Integer quantity, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        if (!cart.getUserId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作");

        cartMapper.updateQuantity(id, quantity);
        cart.setQuantity(quantity);

        Map<String, Object> sku = jdbcTemplate.queryForMap(
            "SELECT id, spu_id, name, price, images FROM product_sku WHERE id = ?", cart.getSkuId());
        log.info("购物车数量修改: cartId={}, quantity={}", id, quantity);
        return toVO(cart, sku);
    }

    @Override
    @Transactional
    public CartVO updateSelected(Long id, Integer selected, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        if (!cart.getUserId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作");

        cartMapper.updateSelected(id, selected);
        cart.setSelected(selected);

        Map<String, Object> sku = jdbcTemplate.queryForMap(
            "SELECT id, spu_id, name, price, images FROM product_sku WHERE id = ?", cart.getSkuId());
        log.info("购物车选中状态修改: cartId={}, selected={}", id, selected);
        return toVO(cart, sku);
    }

    @Override
    @Transactional
    public void selectAll(Long userId, Integer selected) {
        cartMapper.selectAll(userId, selected);
        log.info("购物车全选操作: userId={}, selected={}", userId, selected);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null) throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "购物车项不存在");
        if (!cart.getUserId().equals(userId))
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作");

        cartMapper.deleteById(id);
        log.info("购物车项删除: cartId={}, userId={}", id, userId);
    }

    @Override
    @Transactional
    public void batchDelete(Long userId, List<Long> ids) {
        int rows = cartMapper.deleteBatch(ids, userId);
        log.info("购物车批量删除: userId={}, ids={}, deletedRows={}", userId, ids, rows);
    }

    @Override
    public int count(Long userId) {
        return cartMapper.countByUserId(userId);
    }

    private CartVO toVO(Cart cart, Map<String, Object> sku) {
        BigDecimal price = (BigDecimal) sku.get("price");
        return CartVO.builder()
            .id(cart.getId())
            .userId(cart.getUserId())
            .spuId(((Number) sku.get("spu_id")).longValue())
            .skuId(cart.getSkuId())
            .productName((String) sku.get("name"))
            .productImage((String) sku.get("images"))
            .price(price)
            .quantity(cart.getQuantity())
            .totalPrice(price.multiply(BigDecimal.valueOf(cart.getQuantity())))
            .selected(cart.getSelected())
            .createTime(cart.getCreateTime())
            .build();
    }
}

