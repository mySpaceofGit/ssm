package com.soecode.lyf.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.soecode.lyf.dao.AppointmentDao;
import com.soecode.lyf.dao.BookDao;
import com.soecode.lyf.dto.AppointExecution;
import com.soecode.lyf.entity.Appointment;
import com.soecode.lyf.entity.Book;
import com.soecode.lyf.enums.AppointStateEnum;
import com.soecode.lyf.exception.AppointException;
import com.soecode.lyf.exception.NoNumberException;
import com.soecode.lyf.exception.RepeatAppointException;
import com.soecode.lyf.service.BookService;

@Service
public class BookServiceImpl implements BookService {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	// 娉ㄥ叆Service渚濊禆
	@Autowired
	private BookDao bookDao;

	@Autowired
	private AppointmentDao appointmentDao;


	@Override
	public Book getById(long bookId) {
		return bookDao.queryById(bookId);
	}

	@Override
	public List<Book> getList() {
		return bookDao.queryAll(0, 1000);
	}

	@Override
	@Transactional
	/**
	 * 浣跨敤娉ㄨВ鎺у埗浜嬪姟鏂规硶鐨勪紭鐐癸細 1.寮�鍙戝洟闃熻揪鎴愪竴鑷寸害瀹氾紝鏄庣‘鏍囨敞浜嬪姟鏂规硶鐨勭紪绋嬮鏍�
	 * 2.淇濊瘉浜嬪姟鏂规硶鐨勬墽琛屾椂闂村敖鍙兘鐭紝涓嶈绌挎彃鍏朵粬缃戠粶鎿嶄綔锛孯PC/HTTP璇锋眰鎴栬�呭墺绂诲埌浜嬪姟鏂规硶澶栭儴
	 * 3.涓嶆槸鎵�鏈夌殑鏂规硶閮介渶瑕佷簨鍔★紝濡傚彧鏈変竴鏉′慨鏀规搷浣滐紝鍙鎿嶄綔涓嶉渶瑕佷簨鍔℃帶鍒�
	 */
	public AppointExecution appoint(long bookId, long studentId) {
		try {
			// 鍑忓簱瀛�
			int update = bookDao.reduceNumber(bookId);
			if (update <= 0) {// 搴撳瓨涓嶈冻
				throw new NoNumberException("no number");
			} else {
				// 鎵ц棰勭害鎿嶄綔
				int insert = appointmentDao.insertAppointment(bookId, studentId);
				if (insert <= 0) {// 閲嶅棰勭害
					throw new RepeatAppointException("repeat appoint");
				} else {// 棰勭害鎴愬姛
					Appointment appointment = appointmentDao.queryByKeyWithBook(bookId, studentId);
					return new AppointExecution(bookId, AppointStateEnum.SUCCESS, appointment);
				}
			}
		} catch (NoNumberException e1) {
			throw e1;
		} catch (RepeatAppointException e2) {
			throw e2;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// 鎵�鏈夌紪璇戞湡寮傚父杞崲涓鸿繍琛屾湡寮傚父
			throw new AppointException("appoint inner error:" + e.getMessage());
		}
	}

}
