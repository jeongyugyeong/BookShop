package shopdb;

import java.sql.*; 
import java.util.ArrayList; 

public class BangDAO {

	// �����ͺ��̽� ���� ���� ��� ����
	private static final String JDBC_DRIVER = "org.gjt.mm.mysql.Driver";
	private static final String JDBC_URL = 
			"jdbc:mysql://localhost:3306/shopdb?useUnicode=true&characterEncoding=euckr";
	private static final String USER = "apple";
	private static final String PASSWD = "1234";

	// �����ͺ��̽� ���� ���� ���� ����
	private Connection con = null;
	private PreparedStatement pstmt = null;
	private ResultSet rs = null;

	// JDBC ����̹��� �ε��ϴ� ������
	public BangDAO() {
		// JDBC ����̹� �ε�
		try {
			Class.forName(JDBC_DRIVER);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	// �����ͺ��̽� ���� �޼ҵ�
	public void connect() {
		try {
			// �����ͺ��̽��� ����, Connection ��ü ���� 
			con = DriverManager.getConnection(JDBC_URL, USER, PASSWD);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// �����ͺ��̽� ���� ���� �޼ҵ� 
	public void disconnect() {
		try {
			if(rs != null) rs.close();
			if(pstmt != null) pstmt.close();
			if(con != null) con.close();
		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	// �Խ����� ��� ���ڵ带 ��ȯ �޼���
	public ArrayList<BangEntity> getBangList() {	
		connect();
		ArrayList<BangEntity> list = new ArrayList<BangEntity>();
		
		String SQL = "select * from bang order by ref desc, step";
		try {
			pstmt = con.prepareStatement(SQL);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				BangEntity brd = new BangEntity();
				brd.setNum( rs.getInt("num") );
				brd.setName( rs.getString("name") );
				brd.setPasswd( rs.getString("passwd") );
				brd.setTitle( rs.getString("title") );
				brd.setRegdate( rs.getTimestamp("regdate") );
				brd.setContent( rs.getString("content") );
				brd.setHit( rs.getInt("hit"));
				brd.setRef(rs.getInt("ref"));
				brd.setStep(rs.getInt("step"));
				brd.setDepth(rs.getInt("depth"));
				brd.setChildCount(rs.getInt("childCount"));
				//����Ʈ�� �߰�
				list.add(brd);
			}
					
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally {
			disconnect();
		}
		return list;
	}
	
	

	
	// �� Ű num�� ���ڵ带 ��ȯ�ϴ� �޼���
	public BangEntity getBang(int num) {	
		connect();
		String SQL = "select * from bang where num = ?";
		BangEntity brd = new BangEntity();
		
		try {
			pstmt = con.prepareStatement(SQL);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();			
			rs.next();
			brd.setNum( rs.getInt("num") );
			brd.setName( rs.getString("name") );
			brd.setPasswd( rs.getString("passwd") );
			brd.setTitle( rs.getString("title") );
			brd.setRegdate( rs.getTimestamp("regdate") );
			brd.setContent( rs.getString("content") );
			brd.setHit( rs.getInt("hit"));
			brd.setRef(rs.getInt("ref"));
			brd.setStep(rs.getInt("step"));
			brd.setDepth(rs.getInt("depth"));
			brd.setChildCount(rs.getInt("childCount"));
					
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally {
			disconnect();
		}
		return brd;
	}

	// �Խù� ��� �޼���
	public boolean insertDB(BangEntity board) {
		boolean success = false; 
		connect();
		String sql = "select max(num) as maxnum from bang";
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			rs.next();
			int maxnum = rs.getInt("maxnum") + 1;
			
			sql ="insert into bang values(?, ?, ?, ?, sysdate(), ?, 0, ?, 0, 0, 0)";	
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, maxnum);
			pstmt.setString(2, board.getName());
			pstmt.setString(3, board.getPasswd());
			pstmt.setString(4, board.getTitle());
			pstmt.setString(5, board.getContent());
			pstmt.setInt(6, maxnum);
			pstmt.executeUpdate();
			success = true; 
			
		} catch (SQLException e) {
			e.printStackTrace();
			return success;
		}
		finally {
			disconnect();
		}
		return success;
	}
	
	// ������ ������ ���� �޼���
	public boolean updateDB(BangEntity bang) {
		boolean success = false; 
		connect();		
		String sql ="update bang set name=?, title=?,  content=? where num=?";	
		try {
			pstmt = con.prepareStatement(sql);
			// ���ڷ� ���� GuestBook ��ü�� �̿��� ����ڰ� ������ ���� ������ SQL�� �ϼ�
			pstmt.setString(1, bang.getName());
			pstmt.setString(2, bang.getTitle());
			pstmt.setString(3, bang.getContent());
			pstmt.setInt(4, bang.getNum());
			int rowUdt = pstmt.executeUpdate();
			//System.out.println(rowUdt);
			if (rowUdt == 1) success = true;
		} catch (SQLException e) {
			e.printStackTrace();
			return success;
		}
		finally {
			disconnect();
		}
		return success;
	}
	
	//�Խù��� �亯 ���
	
	@SuppressWarnings("resource")
	public boolean insertReply(BangEntity board) {
		boolean success = false; 
		String sql;
		int num =0, step=0; 
		connect();		
				 
		try {
			sql = "select min(step) as tstep from bang "
				+ "where ref = ? and step > ? and depth <= ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, board.getRef());
			pstmt.setInt(2,  board.getStep());
			pstmt.setInt(3,  board.getDepth());
			rs = pstmt.executeQuery();
			if(rs.next()){
				step = rs.getInt("tstep");
			}
			
			//step ���� ���Ѵ�
			if(step > 0){
				step = rs.getInt("tstep");
				//���� �亯���� step�� 1�� ������Ŵ
				sql = "update bang set step = step + 1 where ref = ? and step >= ? ";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1,  board.getRef());
				pstmt.setInt(2, step);
				pstmt.executeUpdate();
			}else {
				sql = "select max(step) as tstep from bang where ref = ? ";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1,  board.getRef());
				rs = pstmt.executeQuery(); 
				if(rs.next()){
					step = rs.getInt("tstep") + 1;
				}

			}	 
			
			//num (�۹�ȣ) ���� ���Ѵ�. 
			sql = "select max(num) as tnum from bang";
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next()){
				num = rs.getInt("tnum") + 1;
			}
			
			//depth�� ���Ѵ�.
			int depth = board.getDepth() + 1;
			
			//�亯�� ����
			
			sql ="insert into bang values(?, ?, ?, ?, sysdate(), ?, 0, ?, ?, ?, 0)";	
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			pstmt.setString(2, board.getName());
			pstmt.setString(3, board.getPasswd());
			pstmt.setString(4, board.getTitle());
			pstmt.setString(5, board.getContent());
			pstmt.setInt(6, board.getRef());
			pstmt.setInt(7, step);
			pstmt.setInt(8, depth);
			pstmt.executeUpdate();
			
			//childCount ���� 
			for(int r=depth-1; r>=0; r--){
				sql = "select max(step) as tstep from bang "
					+ "where ref = ? and depth = ? and step < ?";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1, board.getRef());
				pstmt.setInt(2, r);
				pstmt.setInt(3, step);
				rs = pstmt.executeQuery();
				
				int maxstep = 0;
				if(rs.next()){
					maxstep = rs.getInt("tstep");					
				}
				
				sql = "update bang set childCount = childCount + 1 "
					+ " where ref = ? and depth = ? and step = ? ";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1,  board.getRef());
				pstmt.setInt(2, r);
				pstmt.setInt(3,  maxstep);
			    pstmt.executeUpdate();
			}
			
			success = true; 
		
		} catch (SQLException e) {
			e.printStackTrace();
			return success;
		}
		finally {
			disconnect();
		}
		return success;
	}
	



	// �����ͺ��̽����� ������ num�� passwd�� ��ġ�ϴ��� �˻��ϴ�  �޼���
	public boolean isPasswd(int num, String passwd) {
		boolean success = false;
		connect();		
		String sql ="select passwd from bang where num=?";
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			rs.next();
			String orgPasswd = rs.getString(1);
			if ( passwd.equals(orgPasswd) ) success = true; 
			System.out.println(success);
					
		} catch (SQLException e) {
			e.printStackTrace();
			return success;
		}
		finally {
			disconnect();
		}
		return success;
	}
	
	
	public boolean updateHit(int num, int hit) {
		boolean success = false; 
		connect();		
		String sql ="update bang set hit=?  where num=?";	
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, hit);
			pstmt.setInt(2, num);
			int rowUdt = pstmt.executeUpdate();
			//System.out.println(rowUdt);
			if (rowUdt == 1) success = true;
		} catch (SQLException e) {
			e.printStackTrace();
			return success;
		}
		finally {
			disconnect();
		}
		return success;
	} 
	
	public int deleteDB(int num) {
		int retval = 0; 
		int ref, step, depth, childCount;
		connect();		
		String sql;
		try {
			sql ="select * from bang where num = ?";
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, num);
			rs = pstmt.executeQuery();
			rs.next();
			ref = rs.getInt("ref");
			step = rs.getInt("step");
			depth = rs.getInt("depth");
			childCount = rs.getInt("childCount");
			
			//�亯�� ���� ���� 
			if(childCount == 0){
				//�ش�� ����
				sql = "delete from bang where num = ? ";
				pstmt = con.prepareStatement(sql);
				pstmt.setInt(1, num);
				int i = pstmt.executeUpdate();
				if(i>0) retval = 0;
			
			    //�θ��� childCount �� ����
				for(int r=depth -1; r>=0; r--){
					sql = "select max(step) as tstep from  bang "
						+ "where ref=? and depth=? and step < ? ";
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, ref);
					pstmt.setInt(2, r);
					pstmt.setInt(3, step);
					rs = pstmt.executeQuery();
					rs.next();
					int maxstep = rs.getInt("tstep");
					
					sql = "update bang set childCount = childCount - 1 "
						+ "where ref = ? and depth = ? and step = ? ";
					pstmt = con.prepareStatement(sql);
					pstmt.setInt(1, ref);
					pstmt.setInt(2, r);
					pstmt.setInt(3,  maxstep);
					pstmt.executeUpdate();						
				}
			} else {
				retval = 1;
			}
		
		} catch (SQLException e) {
			e.printStackTrace();
			return retval;
		}
		finally {
			disconnect();
		}
		System.out.println("retval " + retval);
		return retval;
	}
	
	
	
	
	
	// ����¡ ��� 
	public ArrayList<BangEntity> getBangList(int startRecord, int pageRecordNo) {	
		connect();
		ArrayList<BangEntity> list = new ArrayList<BangEntity>();
		
		String sql = "select * from bang order by ref desc, step limit " 
				   + startRecord + ", " + pageRecordNo;
		System.out.println(sql);
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				BangEntity brd = new BangEntity();
				brd.setNum( rs.getInt("num") );
				brd.setName( rs.getString("name") );
				brd.setPasswd( rs.getString("passwd") );
				brd.setTitle( rs.getString("title") );
				brd.setRegdate( rs.getTimestamp("regdate") );
				brd.setContent( rs.getString("content") );
				brd.setHit( rs.getInt("hit"));
				brd.setRef(rs.getInt("ref"));
				brd.setStep(rs.getInt("step"));
				brd.setDepth(rs.getInt("depth"));
				brd.setChildCount(rs.getInt("childCount"));
				//����Ʈ�� �߰�
				list.add(brd);
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		finally {
			disconnect();
		}
		return list;
	}

	
	public int getTotalRecNo( ){
		int totalRecNum  = 0;
		connect();
		String sql = "select count(*) as cnt from bang";
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if(rs.next())
				totalRecNum = rs.getInt("cnt");
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			disconnect();
		}
		
		return totalRecNum;
	}

}
