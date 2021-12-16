import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Protocol
{

	public static void main(String[] args) throws IOException
	{
		Inventory inventory = new Inventory();

		ServerSocket server = new ServerSocket(Protocol.PORT);

		while (true) {
			System.out.println("Waiting for clients to connect...");
         Socket client = server.accept();
         System.out.println("Client connected.");
         InventoryService service = new InventoryService(client, inventory);
         Thread t = new Thread(service);
         t.start();
		}
		
	}
}

class InventoryService implements Protocol, Runnable
{
	private Socket client;
	private DataInputStream in;
	private DataOutputStream out;
	private Inventory inventory;

	public InventoryService(Socket client, Inventory inventory)
	{
		this.client = client;
		this.inventory = inventory;
	}

	public void run()
	{
		try
		{
			in = new DataInputStream(client.getInputStream());
			out = new DataOutputStream(client.getOutputStream());
			doService();
		} catch (IOException e)
		{
			System.out.println("Client disconnected");
		} finally
		{
			try
			{
				client.close();
			} catch (IOException e)
			{
			}
		}
	}

	public void doService() throws IOException
	{
		int option = 0;
		String item="", thresholdList="";
		int quantity=0, count=0;
		while (option != Protocol.QUIT)
		{
			option = in.readInt();
			switch (option)
			{
			case Protocol.ADD_ITEM:
				try
				{
					System.out.println("I'mready");
					item = in.readUTF();
					quantity = in.readInt();
					inventory.addItem(item, quantity);
					out.writeInt(Protocol.SUCCESS);
					out.flush();
				} catch (IOException e)
				{
					out.writeInt(Protocol.FAILED);
					out.flush();
				}

				break;

			case Protocol.CHECK_ITEM:
				count=0;
				try
				{
					item = in.readUTF();
					count = inventory.checkInventory(item);
					if (count == -1)
					{
						throw new IOException();
					}
					out.writeInt(Protocol.SUCCESS);
					out.writeInt(count);
					out.flush();
					break;
				} catch (IOException e)
				{
					out.writeInt(Protocol.FAILED);
					out.writeInt(count);
					out.flush();
				}

			case Protocol.TAKE_ITEM:
				try
				{
					item = in.readUTF();
					quantity = in.readInt();
					count = inventory.takeItem(item, quantity);
					if (count == -1)
					{
						throw new IOException();
					}
					out.writeInt(Protocol.SUCCESS);
					out.writeInt(count);
					out.flush();
				} catch (IOException e)
				{
					System.out.println("here1");
					out.writeInt(Protocol.FAILED);
					out.writeInt(count);
					out.flush();
				}

				break;

			case Protocol.GET_THRESHOLD:
				try
				{
					quantity = in.readInt();
					thresholdList = inventory.getThreshold(quantity);
					System.out.println(thresholdList);
					out.writeInt(Protocol.SUCCESS);
					out.writeUTF(thresholdList);
					out.flush();
				} catch (IOException e)
				{
					out.writeInt(Protocol.FAILED);
					out.writeUTF(thresholdList);
					out.flush();
				}

				break;

			case Protocol.QUIT:
				try
				{
					out.writeInt(Protocol.QUIT);
					out.flush();
				} catch (IOException e)
				{
					out.writeInt(Protocol.FAILED);
					out.flush();
				}
				break;

			default:
				out.writeInt(Protocol.INVALID_OPTION);
				out.flush();
				break;

			}

		}
	}
}