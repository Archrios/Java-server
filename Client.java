import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Scanner;

public class Client implements Protocol
{
	private static Random rand;

	public static void main(String[] args) throws IOException
	{
		Socket server = new Socket("localhost", Protocol.PORT);
		ClientService service = new ClientService(server);
		Thread t = new Thread(service);
		t.start();
	}
}

class ClientService implements Runnable, Protocol
{
	private Socket server;
	private DataInputStream in;
	private DataOutputStream out;

	public ClientService(Socket server)
	{
		this.server = server;
	}

	public void run()
	{
		while (!Thread.interrupted())
		{
			try
			{
				in = new DataInputStream(server.getInputStream());
				out = new DataOutputStream(server.getOutputStream());
				doService();
				 Thread.sleep((long) ((Math.random() * 400) + 100));
			} catch (UnknownHostException e1)
			{
				e1.printStackTrace();
			} catch (IOException e1)
			{
				try
				{
					server.close();
					System.out.println("Client closed.");
					throw new IOException();
				} catch (IOException e)
				{
					Thread.currentThread().interrupt();
				}
			} catch (InterruptedException e)
			{
			}

		}

	}

	public void doService() throws IOException
	{
		String item;
		int quantity, response, success;
		Scanner s = new Scanner(System.in);
		System.out.println("Input from the following menu what option you would like to do followed by the enter key.");
		System.out.println("1: Add an item");
		System.out.println("2: Check quantity of an item");
		System.out.println("3: Take an item");
		System.out.println("4: Get list of items");
		System.out.println("5: Quit");
		
		int option = s.nextInt();
		s.nextLine();

		switch (option)
		{
		case Protocol.ADD_ITEM:
			System.out.println(
			      "Input the item name followed by enter key, and quantity to be inserted followed by enter key.");
			item = s.nextLine();
			quantity = s.nextInt();
			s.nextLine();
			out.writeInt(Protocol.ADD_ITEM);
			out.writeUTF(item);
			out.writeInt(quantity);
			out.flush();
			success = in.readInt();
			if (success == Protocol.SUCCESS)
			{
				System.out.println(quantity + " " + item + "'s added successfully");
			} else
			{
				System.out.println("action failed");
			}
			break;

		case Protocol.CHECK_ITEM:
			System.out.println("Input the name of item to be checked followed by enter key");
			item = s.nextLine();
			out.writeInt(Protocol.CHECK_ITEM);
			out.writeUTF(item);
			out.flush();
			success = in.readInt();
			response = in.readInt();
			if (success == Protocol.SUCCESS)
			{
				System.out.println("There are " + response + " " + item
				      + " in the inventory");
			} else
			{
				System.out.println("There are no " + item + " in the inventory");
			}
			break;

		case Protocol.TAKE_ITEM:
			System.out.println("Input the item name followed by enter key, and quantity to be taken followed by enter key.");
			item = s.nextLine();
			quantity = s.nextInt();
			s.nextLine();
			out.writeInt(Protocol.TAKE_ITEM);
			out.writeUTF(item);
			out.writeInt(quantity);
			out.flush();
			success = in.readInt();
			response = in.readInt();
			if (success == Protocol.SUCCESS)
			{
				System.out.println(response + " " + item + "'s taken successfully");
			}
			if (success == Protocol.FAILED)
			{
				System.out.println("action failed");
			}
			break;

		case Protocol.GET_THRESHOLD:
			System.out.println("Input the threshold of items to be taken followed by enter key");
			quantity = s.nextInt();
			s.nextLine();
			out.writeInt(Protocol.GET_THRESHOLD);
			out.writeInt(quantity);
			out.flush();
			success = in.readInt();
			String list = in.readUTF();
			System.out.println(list);
			if (success == Protocol.SUCCESS)
			{
				System.out.println(list);
			} else
			{
				System.out.println("action failed");
			}
			break;

		case Protocol.QUIT:
			out.writeInt(Protocol.QUIT);
			out.flush();
			success = in.readInt();
			if (success == Protocol.QUIT)
			{
				throw new IOException();
			} else
			{
				System.out.println("action failed");
			}
			break;

		default:
			out.writeInt(option);
			response = in.readInt();
			if (response == Protocol.INVALID_OPTION)
			{
				System.out.println("That is not a valid option");
			}
			break;

		}

	}
}
