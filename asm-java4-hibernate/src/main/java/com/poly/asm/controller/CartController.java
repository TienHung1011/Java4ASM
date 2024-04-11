package com.poly.asm.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poly.asm.dao.CategoryDao;
import com.poly.asm.dao.ProductDao;
import com.poly.asm.dao.UserDao;
import com.poly.asm.dto.CartDto;
import com.poly.asm.entity.Category;
import com.poly.asm.entity.User;
import com.poly.asm.service.CartService;

@WebServlet("/cart")
public class CartController extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	CartService cartService = new CartService();
	ProductDao productDao = new ProductDao();
	CategoryDao categoryDao = new CategoryDao();
	UserDao userDao = new UserDao();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		List<Category> listCategory = categoryDao.findAll();
		req.setAttribute("listCategory", listCategory);
		
		HttpSession session = req.getSession();
		CartDto cart = (CartDto) session.getAttribute("cart");
		if (cart == null) {
			session.setAttribute("cart", new CartDto());
		}
		
		String action = req.getParameter("action");
		
		if (action.equals("view")) {
			// localhost:8080/asm-java4/cart?action=view
			doGetViewCart(req, resp);
		} else if (action.equals("add")) {
			// localhost:8080/asm-java4/cart?action=add&masp={masp}&soluong={soluong}
			String masp = req.getParameter("masp");
			int soluong = Integer.parseInt(req.getParameter("soluong"));
			doGetAddSP(req, resp, session, masp, soluong);
		} else if (action.equals("remove")) {
			// localhost:8080/asm-java4/cart?action=remove&masp={masp}
			String masp = req.getParameter("masp");
			doGetRemoveSP(req, resp, masp);
		} else if (action.equals("paying")) {
			// localhost:8080/asm-java4/cart?action=paying&phone={phone}&address={address}
			doGetPaying(req, resp, session);
		}
	}
	
	protected void doGetViewCart(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		req.getRequestDispatcher("views/cart.jsp").forward(req, resp);
	}
	
	protected void doGetAddSP(HttpServletRequest req, HttpServletResponse resp, HttpSession session, String masp, int soluong) 
			throws ServletException, IOException {
		CartDto cart = (CartDto) session.getAttribute("cart");
		boolean isUpdate = req.getParameter("isUpdate").equals("1");
		cartService.updateCart(cart, masp, soluong, isUpdate);
		ObjectMapper mapper = new ObjectMapper();
		String cartToJsonString = mapper.writeValueAsString(cart);
		resp.setContentType("application/json");
		PrintWriter out = resp.getWriter();
		out.print(cartToJsonString);
		out.flush();
	}

	protected void doGetRemoveSP(HttpServletRequest req, HttpServletResponse resp, String masp) 
			throws ServletException, IOException {
		req.getRequestDispatcher("views/cart.jsp").forward(req, resp);
	}
	
	protected void doGetPaying(HttpServletRequest req, HttpServletResponse resp, HttpSession session) 
			throws ServletException, IOException {
		
		resp.setContentType("application/json");
		User currentUser = (User) session.getAttribute("user");
		if (currentUser != null) {
			CartDto cart = (CartDto) session.getAttribute("cart");
			String phoneNumber = req.getParameter("phone");
			String address = req.getParameter("address");
			cart.setDienthoai(phoneNumber);
			cart.setDiachi(address);
			cart.setUserId(currentUser.getId());
			if (cartService.insertHoaDon(cart)) {
				session.setAttribute("cart", new CartDto());
				resp.setStatus(200); // 200: ma HTTP success
			} else {
				resp.setStatus(400); // 400: bad request 
			}
		} else {
			resp.setStatus(400); // 400: bad request
		}
	}
}
