# FragmentManager

A fragment manager with several enchancements over the original one:

 - shared element transitions on all APIs
 - fragment pooling
 - annotation-based fragment construction
 - easy route building and execution
 - support for both upstack and backstack
 - fragment locking durning changes
 - fragment tree debugger view
 - no need to call super.on\*() methods
 - passing data between fragments (like startActivityForResult())
 - custom animations for all state changes
 - easy state saving for fields

The lifecycle:

 - create (fragment is ready to use)
 - restore (only if state is not null)
 - attach (fragment's view is attached and laid out)
 - start (activity is started)
 - resume (activity is resumed, fragment is not animating)
 - save
 - pause
 - stop
 - detach
 - destroy

### A sample main fragment

    @FragmentAnnotation(layout = R.layout.fragment_main)
    public class MainFragment extends Fragment {
    
        @Override
        protected void onCreate() {
            if (getState() == null) { // this fragment is not being restored
                add(ContentFragment.class, R.id.container, TransactionMode.Join);
                add(DrawerFragment.class, R.id.drawer, TransactionMode.Join);
            }
        }
    }

### A sample activity with support for navigation with FragmentRoute

    public class MainActivity extends FragmentActivity {
    
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
    
            if (savedInstanceState == null)
                getFragmentManager2().add(MainFragment.class, R.id.root, TransactionMode.Join);
        }
    
        @Override
        protected boolean onNavigate(Fragment fragment, TransactionMode mode) {
            getFragmentManager2().replace(fragment, R.id.root, mode);
            return true;  // navigation was handled by me
        }
    }

### Saving and restoring fragment's state

For example let's pick a random background color and remember it.

    private int color;

    @Override
    protected void onCreate() {
        View view = getView();

        if (getState() == null) { // is it a fresh fragment?
            int[] colors = {Color.RED, Color.BLUE, Color.GRAY, Color.BLACK, Color.YELLOW, Color.GREEN};
            color = colors[new Random().nextInt(colors.length)];
            view.setBackgroundColor(color);
        } else {
            color = getState().getInt(COLOR);
            view.setBackgroundColor(color);
        }
    }

    @Override
    protected Bundle onSaveState() {
        Bundle state = new Bundle();
        state.putInt(COLOR, color);
        return state;
    }

### Shared element transition

Let's create and execute a transition between two fragments. The transition should animate two shared elements - an image and a title.

    DetailFragment detailFragment = Fragment.instantiate(DetailFragment.class, getActivity());
    FragmentTransaction transaction = new FragmentTransaction(getManager(), TransactionMode.Push);
    transaction.replace(detailFragment, R.id.container);
    transaction.addSharedElement(new ViewSharedElement(findViewById(R.id.image), this, detailFragment));
    transaction.addSharedElement(new TextViewSharedElement(findViewById(R.id.title), this, detailFragment));
    transaction.execute();

### Pass data between fragments

Start a fragment with *setTargetFragment()*

    // MainFragment
    DetailFragment detailFragment = Fragment.instantiate(DetailFragment.class, getActivity());
    add(detailFragment, R.id.container, TransactionMode.Add);

Return data using *setResult()*

    // DetailFragment
    int data = 5;
    Bundle bundle = new Bundle();
    bundle.putInt("data", data);
    setResult(bundle);
    getManager().back();

### Save data using annotation

Simple data can be automatically saved and restored using *@State* annotation.

    @State
    int data;
    
This annotation can use setters as well. Simply keep the good, old Java naming convention.

    @State
    String title;
    
    private void setTitle(String title){
        this.title = title;
        getToolbar().setTitle(title);
    }
